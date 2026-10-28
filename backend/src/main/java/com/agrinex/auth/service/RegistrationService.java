package com.agrinex.auth.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.hibernate.exception.ConstraintViolationException;

import com.agrinex.auth.domain.FarmerProfileEntity;
import com.agrinex.auth.domain.UserEntity;
import com.agrinex.auth.repository.FarmerProfileRepository;
import com.agrinex.auth.repository.UserRepository;
import com.agrinex.auth.security.Passwords;
import com.agrinex.common.error.ApiErrorResponse.FieldError;
import com.agrinex.common.error.ApiException;
import com.agrinex.common.error.FieldErrorCode;
import com.agrinex.common.time.AgrinexCalendar;
import com.agrinex.geography.GeographyApi;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates a FARMER account and signs the farmer in (spec section 25; ADR-0016 item 3, ADR-0047). The email is
 * stored in lower case and starts unverified; its verification link is sent after the commit (ADR-0048 item 4).
 * A taken phone number or email answers 409 naming each taken field (accepted residual risk SR-01, AR-02).
 */
@Service
public class RegistrationService {

	private static final String TAKEN = "is already registered";

	/** Unique constraints of V5__accounts_and_admin_audit.sql. */
	private static final String PHONE_CONSTRAINT = "uq_users_phone_number";

	private static final String EMAIL_CONSTRAINT = "uq_users_email";

	private final UserRepository users;

	private final FarmerProfileRepository profiles;

	private final GeographyApi geography;

	private final Passwords passwords;

	private final EmailVerificationService emailVerification;

	private final SessionService sessions;

	private final AgrinexCalendar calendar;

	RegistrationService(UserRepository users, FarmerProfileRepository profiles, GeographyApi geography,
			Passwords passwords, EmailVerificationService emailVerification, SessionService sessions,
			AgrinexCalendar calendar) {
		this.users = users;
		this.profiles = profiles;
		this.geography = geography;
		this.passwords = passwords;
		this.emailVerification = emailVerification;
		this.sessions = sessions;
		this.calendar = calendar;
	}

	@Transactional
	public SessionGrant register(Registration registration) {
		String email = registration.email().toLowerCase(Locale.ROOT);
		if (registration.defaultDistrictId() != null
				&& this.geography.findDistrict(registration.defaultDistrictId()).isEmpty()) {
			throw ApiException.referenceNotFound("defaultDistrictId");
		}
		requireUnregistered(this.users.existsByPhoneNumber(registration.phoneNumber()),
				this.users.existsByEmail(email));
		UserEntity user = UserEntity.farmer(registration.phoneNumber(), email,
				this.passwords.hash(registration.password()), this.calendar.now());
		try {
			this.users.saveAndFlush(user);
		}
		catch (DataIntegrityViolationException raced) {
			// A concurrent registration took the number or the email after the checks above.
			String constraint = (raced.getCause() instanceof ConstraintViolationException violation)
					? violation.getConstraintName() : null;
			requireUnregistered(PHONE_CONSTRAINT.equals(constraint), EMAIL_CONSTRAINT.equals(constraint));
			throw raced;
		}
		this.profiles.save(new FarmerProfileEntity(user.getId(), registration.displayName(),
				registration.defaultDistrictId()));
		this.emailVerification.issue(user);
		return this.sessions.open(user);
	}

	private static void requireUnregistered(boolean phoneTaken, boolean emailTaken) {
		List<FieldError> taken = new ArrayList<>();
		if (phoneTaken) {
			taken.add(new FieldError("phoneNumber", FieldErrorCode.ALREADY_EXISTS, TAKEN));
		}
		if (emailTaken) {
			taken.add(new FieldError("email", FieldErrorCode.ALREADY_EXISTS, TAKEN));
		}
		if (!taken.isEmpty()) {
			throw ApiException.conflict("The mobile number or the email address is already registered.", taken);
		}
	}

}
