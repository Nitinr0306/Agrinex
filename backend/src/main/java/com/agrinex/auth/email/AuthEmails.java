package com.agrinex.auth.email;

import java.time.Duration;
import java.util.Locale;
import java.util.UUID;

import com.agrinex.auth.config.AuthProperties;
import com.agrinex.auth.config.OriginProperties;

import org.springframework.stereotype.Component;

/**
 * Composes the three account emails (ADR-0048 item 8) as plain text in English, Marathi and Hindi, in that order,
 * in every message (ADR-0052 item 7): the contract carries no language preference, so one trilingual message is
 * the only form every recipient can read. The subject follows the language chooser's "EN · MR · HI" pattern. The
 * link appears once, after the English text; the Marathi and Hindi paragraphs refer to it. Links are built on the
 * configured public origin, never on a request's Host header, and carry the token in the URL fragment, so it never
 * reaches a server log or a Referer header (ADR-0048 item 2). Marathi and Hindi follow the glossary and the
 * catalogue wording of role file 02 (ADR-0046); they were written by Claude, not reviewed by a native speaker.
 */
@Component
class AuthEmails {

	static final String VERIFY_EMAIL_LINK = "/verify-email#token=";

	static final String RESET_PASSWORD_LINK = "/reset-password#token=";

	private final OriginProperties origins;

	private final AuthProperties auth;

	AuthEmails(OriginProperties origins, AuthProperties auth) {
		this.origins = origins;
		this.auth = auth;
	}

	EmailMessage compose(AuthEmailRequested request) {
		return switch (request.kind()) {
			case EMAIL_VERIFICATION -> verification(request, this.auth.emailVerificationTtl());
			case PASSWORD_RESET -> reset(request, this.auth.passwordResetTtl());
			case PASSWORD_CHANGED -> message(request,
					"Your AgriNex password was changed · पासवर्ड बदलला · पासवर्ड बदल गया", """
							The password of your AgriNex account was changed, and every signed-in session was signed \
							out.

							If you did not make this change, request a password reset at once.

							मराठी: तुमच्या AgriNex खात्याचा पासवर्ड बदलला आहे, आणि जिथे जिथे साइन इन केले होते तिथून \
							साइन आउट केले आहे. हा बदल तुम्ही केला नसल्यास लगेच रीसेट लिंक मागवा.

							हिंदी: आपके AgriNex खाते का पासवर्ड बदल गया है, और जहाँ-जहाँ साइन इन किया गया था, वहाँ से \
							साइन आउट कर दिया गया है। अगर यह बदलाव आपने नहीं किया है, तो तुरंत रीसेट लिंक मँगवाएँ।
							""");
		};
	}

	private EmailMessage verification(AuthEmailRequested request, Duration ttl) {
		return message(request, "Confirm your email address for AgriNex · ईमेल पडताळणी · ईमेल सत्यापन", """
				Confirm the email address of your AgriNex account by opening this link:

				%s

				The link works once and expires in %s. If you did not create an AgriNex account, you can ignore \
				this email.

				मराठी: तुमच्या AgriNex खात्याचा ईमेल पत्ता पडताळण्यासाठी वरील लिंक उघडा. ही लिंक एकदाच चालते आणि \
				%s तिची मुदत संपते. तुम्ही AgriNex खाते तयार केले नसल्यास या ईमेलकडे दुर्लक्ष करा.

				हिंदी: अपने AgriNex खाते का ईमेल पता सत्यापित करने के लिए ऊपर दिया गया लिंक खोलें। यह लिंक एक ही बार \
				काम करता है और %s इसकी अवधि खत्म हो जाती है। अगर आपने AgriNex खाता नहीं बनाया है, तो इस ईमेल को \
				अनदेखा करें।
				""".formatted(link(VERIFY_EMAIL_LINK, request.token()), describe(ttl), describeMarathi(ttl),
				describeHindi(ttl)));
	}

	private EmailMessage reset(AuthEmailRequested request, Duration ttl) {
		return message(request, "Reset your AgriNex password · पासवर्ड पुन्हा सेट करा · पासवर्ड दोबारा सेट करें", """
				Someone asked to reset the password of the AgriNex account that uses this email address. To choose \
				a new password, open this link:

				%s

				The link works once and expires in %s. If you did not ask for this, ignore this email: your \
				password stays the same.

				मराठी: हा ईमेल पत्ता वापरणाऱ्या AgriNex खात्याचा पासवर्ड पुन्हा सेट करण्याची विनंती कोणीतरी केली आहे. \
				नवीन पासवर्ड निवडण्यासाठी वरील लिंक उघडा. ही लिंक एकदाच चालते आणि %s तिची मुदत संपते. ही विनंती \
				तुम्ही केली नसल्यास या ईमेलकडे दुर्लक्ष करा: तुमचा पासवर्ड बदलणार नाही.

				हिंदी: इस ईमेल पते वाले AgriNex खाते का पासवर्ड दोबारा सेट करने का अनुरोध किसी ने किया है। नया पासवर्ड \
				चुनने के लिए ऊपर दिया गया लिंक खोलें। यह लिंक एक ही बार काम करता है और %s इसकी अवधि खत्म हो जाती \
				है। अगर यह अनुरोध आपने नहीं किया है, तो इस ईमेल को अनदेखा करें: आपका पासवर्ड नहीं बदलेगा।
				""".formatted(link(RESET_PASSWORD_LINK, request.token()), describe(ttl), describeMarathi(ttl),
				describeHindi(ttl)));
	}

	private String link(String path, String token) {
		return this.origins.publicOrigin() + path + token;
	}

	private static EmailMessage message(AuthEmailRequested request, String subject, String text) {
		String idempotencyKey = "agrinex-" + request.kind().name().toLowerCase(Locale.ROOT) + "-"
				+ UUID.randomUUID();
		return new EmailMessage(request.kind(), request.recipient(), subject, text, idempotencyKey);
	}

	/** "24 hours" or "30 minutes": link lifetimes are whole hours or whole minutes. */
	static String describe(Duration lifetime) {
		if (wholeHours(lifetime)) {
			return lifetime.toHours() + (lifetime.toHours() == 1 ? " hour" : " hours");
		}
		return lifetime.toMinutes() + (lifetime.toMinutes() == 1 ? " minute" : " minutes");
	}

	/** "24 तासांत" or "30 मिनिटांत" ("within …"), with Latin digits (PDD-06). */
	static String describeMarathi(Duration lifetime) {
		if (wholeHours(lifetime)) {
			return lifetime.toHours() + (lifetime.toHours() == 1 ? " तासात" : " तासांत");
		}
		return lifetime.toMinutes() + (lifetime.toMinutes() == 1 ? " मिनिटात" : " मिनिटांत");
	}

	/** "24 घंटे में" or "30 मिनट में" ("within …"), with Latin digits (PDD-06). */
	static String describeHindi(Duration lifetime) {
		return wholeHours(lifetime) ? lifetime.toHours() + " घंटे में" : lifetime.toMinutes() + " मिनट में";
	}

	private static boolean wholeHours(Duration lifetime) {
		return lifetime.toMinutesPart() == 0 && lifetime.toHours() > 0;
	}

}
