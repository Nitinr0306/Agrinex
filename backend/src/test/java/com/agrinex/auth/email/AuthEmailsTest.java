package com.agrinex.auth.email;

import java.time.Duration;
import java.util.regex.Pattern;

import com.agrinex.auth.config.AuthProperties;
import com.agrinex.auth.config.OriginProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The trilingual account emails (ADR-0048 item 8, ADR-0052 item 7). The token and the recipient are SYNTHETIC.
 */
class AuthEmailsTest {

	static final String TOKEN = "SYNTHETICtokenSYNTHETICtokenSYNTHETICtoken0";

	static final String RECIPIENT = "synthetic.farmer@example.com";

	static final Pattern DEVANAGARI_DIGIT = Pattern.compile("[\\u0966-\\u096F]");

	final AuthEmails emails = new AuthEmails(new OriginProperties("http://localhost:3000", null),
			new AuthProperties("unused", Duration.ofMinutes(15), Duration.ofDays(30), 12, Duration.ofHours(24),
					Duration.ofMinutes(30)));

	@Test
	void verificationEmailCarriesTheLinkOnceWithTheLifetimeInEachLanguage() {
		EmailMessage message = this.emails.compose(AuthEmailRequested.verification(RECIPIENT, TOKEN));

		assertThat(message.subject()).isEqualTo("Confirm your email address for AgriNex · ईमेल पडताळणी · ईमेल सत्यापन");
		assertThat(occurrences(message.text(), "http://localhost:3000/verify-email#token=" + TOKEN)).isEqualTo(1);
		assertThat(occurrences(message.text(), TOKEN)).isEqualTo(1);
		assertThat(message.text()).contains("The link works once and expires in 24 hours.")
			.contains("मराठी: ")
			.contains("ही लिंक एकदाच चालते आणि 24 तासांत तिची मुदत संपते.")
			.contains("हिंदी: ")
			.contains("यह लिंक एक ही बार काम करता है और 24 घंटे में इसकी अवधि खत्म हो जाती है।");
		assertThat(message.text().indexOf("मराठी: ")).isGreaterThan(message.text().indexOf(TOKEN));
		assertThat(message.text().indexOf("हिंदी: ")).isGreaterThan(message.text().indexOf("मराठी: "));
		assertPlainAndPrivate(message);
	}

	@Test
	void resetEmailCarriesTheLinkOnceWithTheLifetimeInEachLanguage() {
		EmailMessage message = this.emails.compose(AuthEmailRequested.passwordReset(RECIPIENT, TOKEN));

		assertThat(message.subject())
			.isEqualTo("Reset your AgriNex password · पासवर्ड पुन्हा सेट करा · पासवर्ड दोबारा सेट करें");
		assertThat(occurrences(message.text(), "http://localhost:3000/reset-password#token=" + TOKEN)).isEqualTo(1);
		assertThat(occurrences(message.text(), TOKEN)).isEqualTo(1);
		assertThat(message.text()).contains("The link works once and expires in 30 minutes.")
			.contains("ही लिंक एकदाच चालते आणि 30 मिनिटांत तिची मुदत संपते.")
			.contains("यह लिंक एक ही बार काम करता है और 30 मिनट में इसकी अवधि खत्म हो जाती है।")
			.contains("your password stays the same")
			.contains("तुमचा पासवर्ड बदलणार नाही")
			.contains("आपका पासवर्ड नहीं बदलेगा");
		assertPlainAndPrivate(message);
	}

	@Test
	void passwordChangedEmailHasNoLinkAndSaysWhatToDoInEachLanguage() {
		EmailMessage message = this.emails.compose(AuthEmailRequested.passwordChanged(RECIPIENT));

		assertThat(message.subject()).isEqualTo("Your AgriNex password was changed · पासवर्ड बदलला · पासवर्ड बदल गया");
		assertThat(message.text()).doesNotContain("http").doesNotContain("token")
			.contains("request a password reset at once")
			.contains("लगेच रीसेट लिंक मागवा")
			.contains("तुरंत रीसेट लिंक मँगवाएँ");
		assertPlainAndPrivate(message);
	}

	@Test
	void lifetimesAreDescribedInWholeHoursOrMinutesInEachLanguage() {
		assertThat(AuthEmails.describe(Duration.ofHours(1))).isEqualTo("1 hour");
		assertThat(AuthEmails.describeMarathi(Duration.ofHours(1))).isEqualTo("1 तासात");
		assertThat(AuthEmails.describeMarathi(Duration.ofHours(24))).isEqualTo("24 तासांत");
		assertThat(AuthEmails.describeMarathi(Duration.ofMinutes(1))).isEqualTo("1 मिनिटात");
		assertThat(AuthEmails.describeHindi(Duration.ofHours(24))).isEqualTo("24 घंटे में");
		assertThat(AuthEmails.describeHindi(Duration.ofMinutes(90))).isEqualTo("90 मिनट में");
		assertThat(AuthEmails.describe(Duration.ofMinutes(90))).isEqualTo("90 minutes");
	}

	/** Plain text with Latin digits (PDD-06), no recipient address in the body, one idempotency key per message. */
	private static void assertPlainAndPrivate(EmailMessage message) {
		assertThat(message.to()).isEqualTo(RECIPIENT);
		assertThat(message.text()).doesNotContain(RECIPIENT).doesNotContain("<").doesNotContain("\\");
		assertThat(DEVANAGARI_DIGIT.matcher(message.subject() + message.text()).find()).isFalse();
		assertThat(message.idempotencyKey()).startsWith("agrinex-" + message.kind().name().toLowerCase() + "-");
	}

	private static int occurrences(String text, String part) {
		return text.split(Pattern.quote(part), -1).length - 1;
	}

}
