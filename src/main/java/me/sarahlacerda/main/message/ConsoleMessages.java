package me.sarahlacerda.main.message;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public enum ConsoleMessages {
    ALREADY_REGISTERED("already_registered"),
    EMAIL_ALREADY_SENT_WAIT_SECONDS("email_already_sent_wait_seconds"),
    EMAIL_ALREADY_SENT_WAIT_MINUTES("email_already_sent_wait_minutes"),
    EMAIL_NOT_VALID("email_not_valid"),
    EMAIL_NOT_ALLOWED("email_not_allowed"),
    NEW_OTP_GENERATED("new_otp_generated"),
    NO_PASSWORD_SET_YET("no_password_set_yet"),
    MUST_VERIFY_EMAIL_BEFORE_RESETTING_PASSWORD("must_verify_email_before_resetting_password"),
    PASSWORDS_DO_NOT_MATCH("passwords_do_not_match"),
    INVALID_PASSWORD_ARGUMENTS("invalid_password_arguments"),
    FORGOT_PASSWORD_HINT("forgot_password_hint"),
    MUST_VERIFY_EMAIL_BEFORE_SETTING_PASSWORD("must_verify_email_before_setting_password"),
    PASSWORD_CREATED_WELCOME("password_created_welcome"),
    LOGIN_BACK_HINT("login_back_hint"),
    INVALID_REGISTER_ARGUMENTS("invalid_register_arguments"),
    INVALID_CODE_ARGUMENTS("invalid_code_arguments"),
    EMAIL_VERIFIED("email_verified"),
    NOT_AUTHENTICATED("not_authenticated"),
    SERVER_IS_FULL("server_is_full"),
    WELCOME_BACK_ALREADY_REGISTERED("welcome_back_already_registered"),
    WELCOME_BACK_NO_PASSWORD_SET("welcome_back_no_password_set"),
    WELCOME_NEW_PLAYER("welcome_new_player"),
    KICKED_TO_MAKE_ROOM("kicked_to_make_room"),
    YOU_ARE_IN("you_are_in"),
    WRONG_PASSWORD("wrong_password"),
    MUST_VERIFY_EMAIL_BEFORE_LOGIN("must_verify_email_before_login"),
    INVALID_LOGIN_ARGUMENTS("invalid_login_arguments"),
    INVALID_CODE_ENTERED("invalid_code_entered"),
    PASSWORD_DOES_NOT_MEET_REQUIREMENTS("password_does_not_meet_requirements"),
    PASSWORD_REQUIREMENTS("password_requirements"),
    EMAIL_SENT("email_sent"),
    EMAIL_ALREADY_REGISTERED("email_already_registered");

    private final String reference;
    private String message;

    public static Map<String, ConsoleMessages> consoleMessages;

    ConsoleMessages(String reference) {
        this.reference = reference;
    }

    public String getReference() {
        return reference;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public static String get(ConsoleMessages consoleMessage) {
        return consoleMessages.get(consoleMessage.getReference()).getMessage();
    }

    public static void initConsoleMessages(MessageManager messageManager) {
        consoleMessages = new HashMap<>();

        Arrays.stream(ConsoleMessages.values()).forEach(consoleMessage -> {
            consoleMessage.setMessage(messageManager.getMessage(consoleMessage.getReference()));
            consoleMessages.put(consoleMessage.getReference(), consoleMessage);
        });
    }
}
