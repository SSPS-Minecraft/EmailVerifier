package me.sarahlacerda.main;

import me.sarahlacerda.main.listener.PlayerLoginListener;
import me.sarahlacerda.main.service.PasswordService;
import me.sarahlacerda.main.service.PlayerVerificationService;
import me.sarahlacerda.main.config.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
            case "authenticate" -> registerEmail(commandSender, args);
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
                       player.sendMessage(ChatColor.GREEN + "You're in!. Welcome back! :D");
                   } else {
                       player.sendMessage(ChatColor.RED + "Wrong password! Please try again. If you forgot your password, you can reset it by using /resetpassword");
                   }
                } else if (playerLoginListener.alreadyEmailVerifiedButHasNoPasswordSet(player.getUniqueId())) {
                    player.sendMessage(ChatColor.RED + "You haven't set a password yet. Please use /password <your password> <confirm password> to create your password before logging in");
                    return false;
                }
                player.sendMessage(ChatColor.RED + "You must verify your email before you can login! Please use /register <your email address> to do so!");
            }
        } else {
            commandSender.sendMessage(ChatColor.RED + "Invalid Arguments. Please use /login <your password> to login");
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

                commandSender.sendMessage(ChatColor.GREEN + "A new One Time Code has been sent to the e-mail address you provided, please use /code <code> with the code received in your e-mail to reset your password");
            } else if (playerLoginListener.alreadyEmailVerifiedButHasNoPasswordSet(player.getUniqueId())) {
                player.sendMessage(ChatColor.RED + "You haven't set a password yet. Please use /password <your password> <confirm password> to create your password");
                return false;
            }
            player.sendMessage(ChatColor.RED + "You must verify your email before you can reset a password! Please use /register <your email address> to do so!");
        }
        return false;
    }

    private boolean createPassword(CommandSender commandSender, String[] args) {
        if (commandSender instanceof Player) {
            if (argumentsValidWhenExpectingTwoArguments(args)) {
                if (passwordsDoNotMatch(args)) {
                    commandSender.sendMessage(ChatColor.RED + "Passwords do not match! Please try again. Usage: /password <your password> <confirm password>");
                    return false;
                }
                return createPasswordForPlayer((Player) commandSender, args[0]);
            } else {
                commandSender.sendMessage(ChatColor.RED + "Invalid Arguments. Please use /password <your password> <confirm password>");
            }
        }
        return false;
    }

    private boolean passwordsDoNotMatch(String[] args) {
        return !args[0].equals(args[1]);
    }

    private boolean createPasswordForPlayer(Player player, String password) {
        if (playerLoginListener.isAlreadyRegistered(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You are already registered! Please your /login <your password> to login");
            player.sendMessage(ChatColor.DARK_PURPLE + "If you forgot your password, please use /resetpassword");
        } else if (playerLoginListener.alreadyEmailVerifiedButHasNoPasswordSet(player.getUniqueId())) {
            setPasswordForPlayerAndAuthenticateThem(player, password);
            return true;
        }

        player.sendMessage(ChatColor.RED + "You must verify your email before you can create a password! Please use /register <your email address> to do so!");
        return false;
    }

    private void setPasswordForPlayerAndAuthenticateThem(Player player, String password) {
        playerLoginListener.getOnlineUnauthenticatedPlayers().remove(player);
        configManager.setPasswordForPlayer(player.getUniqueId(), passwordService.generateHashFor(password));
        unHidePlayer(player);
        player.sendMessage(ChatColor.GREEN + "Your password has been created successfully!. Thank you and Welcome! :D");
        player.sendMessage(ChatColor.GREEN + "Every time you log back into this server, just use /login <your password> to login");
    }

    private boolean registerEmail(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            if (argumentsValidWhenExpectingOnlyOneArgument(args)) {
                Player player = (Player) sender;
                return playerVerificationService.createTask(player, args[0]);
            } else {
                sender.sendMessage(ChatColor.RED + "Invalid Arguments. Please use /authenticate [email]");
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
                sender.sendMessage(ChatColor.RED + "Invalid Arguments. Please use /code [code]");
            }
        }
        return true;
    }

    private void validateCodeForPlayer(Player player, int code) {
        if (codeIsValidForPlayer(player, code)) {
            confirmEmailVerification(player, code);
        } else {
            player.sendMessage(ChatColor.RED + "You have entered an invalid code");
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
        player.sendMessage(ChatColor.GREEN + "You have been verified!. Please use /password <your new password> <confirm your new password>> to register a login password!");

        //Add the authenticated player to the file with their email they used to authenticate
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
