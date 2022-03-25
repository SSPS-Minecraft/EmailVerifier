package me.sarahlacerda.main;

import me.sarahlacerda.main.config.ConfigManager;
import me.sarahlacerda.main.listener.PlayerLoginListener;
import me.sarahlacerda.main.service.PasswordService;
import me.sarahlacerda.main.service.PlayerVerificationService;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static me.sarahlacerda.main.ConsoleMessages.ALREADY_REGISTERED;
import static me.sarahlacerda.main.ConsoleMessages.EMAIL_VERIFIED;
import static me.sarahlacerda.main.ConsoleMessages.FORGOT_PASSWORD_HINT;
import static me.sarahlacerda.main.ConsoleMessages.INVALID_CODE_ARGUMENTS;
import static me.sarahlacerda.main.ConsoleMessages.INVALID_CODE_ENTERED;
import static me.sarahlacerda.main.ConsoleMessages.INVALID_LOGIN_ARGUMENTS;
import static me.sarahlacerda.main.ConsoleMessages.INVALID_PASSWORD_ARGUMENTS;
import static me.sarahlacerda.main.ConsoleMessages.INVALID_REGISTER_ARGUMENTS;
import static me.sarahlacerda.main.ConsoleMessages.LOGIN_BACK_HINT;
import static me.sarahlacerda.main.ConsoleMessages.MUST_VERIFY_EMAIL_BEFORE_LOGIN;
import static me.sarahlacerda.main.ConsoleMessages.MUST_VERIFY_EMAIL_BEFORE_RESETTING_PASSWORD;
import static me.sarahlacerda.main.ConsoleMessages.MUST_VERIFY_EMAIL_BEFORE_SETTING_PASSWORD;
import static me.sarahlacerda.main.ConsoleMessages.NEW_OTP_GENERATED;
import static me.sarahlacerda.main.ConsoleMessages.NO_PASSWORD_SET_YET;
import static me.sarahlacerda.main.ConsoleMessages.PASSWORDS_DO_NOT_MATCH;
import static me.sarahlacerda.main.ConsoleMessages.PASSWORD_CREATED_WELCOME;
import static me.sarahlacerda.main.ConsoleMessages.WRONG_PASSWORD;
import static me.sarahlacerda.main.ConsoleMessages.YOU_ARE_IN;
import static me.sarahlacerda.main.ConsoleMessages.get;

public class CommandOrchestrator implements CommandExecutor {
    private final PlayerVerificationService playerVerificationService;
    private final PlayerLoginListener playerLoginListener;
    private final PasswordService passwordService;
    private final ConfigManager configManager;

    public CommandOrchestrator(PlayerVerificationService playerVerificationService, PlayerLoginListener playerLoginListener, PasswordService passwordService, ConfigManager configManager) {
        this.playerVerificationService = playerVerificationService;
        this.playerLoginListener = playerLoginListener;
        this.passwordService = passwordService;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        return switch (command.getName().toLowerCase()) {
            case "register" -> registerEmail(commandSender, args);
            case "code" -> verifyCode(commandSender, args);
            case "password" -> createPassword(commandSender, args);
            case "resetpassword" -> resetPassword(commandSender, args);
            case "login" -> login(commandSender, args);
            default -> true;
        };
    }

    private boolean login(CommandSender commandSender, String[] args) {
        if (argumentsValidWhenExpectingOnlyOneArgument(args)) {
            if (commandSender instanceof Player player) {
                if (playerLoginListener.isAlreadyRegistered(player.getUniqueId())) {
                   if (passwordsMatch(args[0], player)) {
                       playerLoginListener.getOnlineUnauthenticatedPlayers().remove(player);
                       unHidePlayer(player);
                       player.sendMessage(ChatColor.GREEN + get(YOU_ARE_IN));
                   } else {
                       player.sendMessage(ChatColor.RED + get(WRONG_PASSWORD));
                   }
                } else if (playerLoginListener.alreadyEmailVerifiedButHasNoPasswordSet(player.getUniqueId())) {
                    player.sendMessage(ChatColor.RED + get(NO_PASSWORD_SET_YET));
                    return false;
                }
                player.sendMessage(ChatColor.RED + get(MUST_VERIFY_EMAIL_BEFORE_LOGIN));
            }
        } else {
            commandSender.sendMessage(ChatColor.RED + get(INVALID_LOGIN_ARGUMENTS));
        }

        return false;
    }

    private boolean passwordsMatch(String passwordProvided, Player player) {
        return passwordService.validate(passwordProvided, configManager.getPlayerPassword(player.getUniqueId()));
    }

    private boolean resetPassword(CommandSender commandSender, String[] args) {
        if (commandSender instanceof Player player) {
            if (playerLoginListener.isAlreadyRegistered(player.getUniqueId())) {
                configManager.setPasswordForPlayer(player.getUniqueId(), null);
                playerVerificationService.createTask(player, configManager.getPlayerEmail(player.getUniqueId()));

                commandSender.sendMessage(ChatColor.GREEN + get(NEW_OTP_GENERATED));
            } else if (playerLoginListener.alreadyEmailVerifiedButHasNoPasswordSet(player.getUniqueId())) {
                player.sendMessage(ChatColor.RED + get(NO_PASSWORD_SET_YET));
                return false;
            }
            player.sendMessage(ChatColor.RED + get(MUST_VERIFY_EMAIL_BEFORE_RESETTING_PASSWORD));
        }
        return false;
    }

    private boolean createPassword(CommandSender commandSender, String[] args) {
        if (commandSender instanceof Player player) {
            if (argumentsValidWhenExpectingTwoArguments(args)) {
                if (passwordsDoNotMatch(args)) {
                    player.sendMessage(ChatColor.RED + get(PASSWORDS_DO_NOT_MATCH));
                    return false;
                }
                return createPasswordForPlayer(player, args[0]);
            } else {
                commandSender.sendMessage(ChatColor.RED + get(INVALID_PASSWORD_ARGUMENTS));
            }
        }
        return false;
    }

    private boolean passwordsDoNotMatch(String[] args) {
        return !args[0].equals(args[1]);
    }

    private boolean createPasswordForPlayer(Player player, String password) {
        if (playerLoginListener.isAlreadyRegistered(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + get(ALREADY_REGISTERED));
            player.sendMessage(ChatColor.DARK_PURPLE + get(FORGOT_PASSWORD_HINT));
        } else if (playerLoginListener.alreadyEmailVerifiedButHasNoPasswordSet(player.getUniqueId())) {
            setPasswordForPlayerAndAuthenticateThem(player, password);
            return true;
        }

        player.sendMessage(ChatColor.RED + get(MUST_VERIFY_EMAIL_BEFORE_SETTING_PASSWORD));
        return false;
    }

    private void setPasswordForPlayerAndAuthenticateThem(Player player, String password) {
        playerLoginListener.getOnlineUnauthenticatedPlayers().remove(player);
        configManager.setPasswordForPlayer(player.getUniqueId(), passwordService.generateHashFor(password));
        unHidePlayer(player);
        player.sendMessage(ChatColor.GREEN + get(PASSWORD_CREATED_WELCOME));
        player.sendMessage(ChatColor.GREEN + get(LOGIN_BACK_HINT));
    }

    private boolean registerEmail(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            if (argumentsValidWhenExpectingOnlyOneArgument(args)) {
                Player player = (Player) sender;
                return playerVerificationService.createTask(player, args[0]);
            } else {
                sender.sendMessage(ChatColor.RED + get(INVALID_REGISTER_ARGUMENTS));
            }
        }
        return false;
    }

    private boolean verifyCode(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            if (argumentsValidWhenExpectingOnlyOneArgument(args)) {
                Player p = (Player) sender;
                int code = Integer.parseInt(args[0]);
                validateCodeForPlayer(p, code);
            } else {
                sender.sendMessage(ChatColor.RED + get(INVALID_CODE_ARGUMENTS));
            }
        }
        return true;
    }

    private void validateCodeForPlayer(Player player, int code) {
        if (codeIsValidForPlayer(player, code)) {
            confirmEmailVerification(player, code);
        } else {
            player.sendMessage(ChatColor.RED + get(INVALID_CODE_ENTERED));
        }
    }

    private boolean argumentsValidWhenExpectingOnlyOneArgument(String[] args) {
        return args.length == 1;
    }

    private boolean argumentsValidWhenExpectingTwoArguments(String[] args) {
        return args.length == 2;
    }

    private boolean codeIsValidForPlayer(Player player, int code) {
        return playerVerificationService.getCodeRequests().containsKey(code)
                && playerVerificationService.getCodeRequests().get(code).getPlayer().getUniqueId().equals(player.getUniqueId());
    }

    private void confirmEmailVerification(Player player, int code) {
        player.sendMessage(ChatColor.GREEN + get(EMAIL_VERIFIED));

        configManager.setEmailForPlayer(player.getUniqueId(), PlayerLoginListener.emailCode.get(code));

        PlayerLoginListener.emailCode.remove(code);
        playerVerificationService.getCodeRequests().remove(code);
    }

    private void unHidePlayer(Player player) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.showPlayer(p);
        }
        player.setPlayerListName(player.getDisplayName());
    }
}
