package me.sarahlacerda.main.executor;

import me.sarahlacerda.main.service.PlayerLoginService;
import me.sarahlacerda.main.service.PlayerPasswordService;
import me.sarahlacerda.main.service.PlayerVerificationService;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static me.sarahlacerda.main.message.ConsoleMessages.INVALID_CODE_ARGUMENTS;
import static me.sarahlacerda.main.message.ConsoleMessages.INVALID_LOGIN_ARGUMENTS;
import static me.sarahlacerda.main.message.ConsoleMessages.INVALID_PASSWORD_ARGUMENTS;
import static me.sarahlacerda.main.message.ConsoleMessages.INVALID_REGISTER_ARGUMENTS;
import static me.sarahlacerda.main.message.ConsoleMessages.get;

public class CommandOrchestrator implements CommandExecutor {
    private final PlayerVerificationService playerVerificationService;
    private final PlayerLoginService playerLoginService;
    private final PlayerPasswordService playerPasswordService;

    public CommandOrchestrator(PlayerVerificationService playerVerificationService, PlayerLoginService playerLoginService, PlayerPasswordService playerPasswordService) {
        this.playerVerificationService = playerVerificationService;
        this.playerLoginService = playerLoginService;
        this.playerPasswordService = playerPasswordService;
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

    private boolean registerEmail(CommandSender sender, String[] args) {
        if (sender instanceof Player player) {

            if (invalidArguments(args, 1)) {
                sender.sendMessage(ChatColor.RED + get(INVALID_REGISTER_ARGUMENTS));
                return false;
            }

            return playerVerificationService.verifyPlayer(player, args[0]);
        }

        return true;
    }

    private boolean verifyCode(CommandSender sender, String[] args) {
        if (sender instanceof Player player) {

            if (invalidArguments(args, 1)) {
                sender.sendMessage(ChatColor.RED + get(INVALID_CODE_ARGUMENTS));
                return false;
            }

            return playerVerificationService.validateCodeForPlayer(player, Integer.parseInt(args[0]));
        }

        return true;
    }

    private boolean createPassword(CommandSender commandSender, String[] args) {
        if (commandSender instanceof Player player) {

            if (invalidArguments(args, 2)) {
                commandSender.sendMessage(ChatColor.RED + get(INVALID_PASSWORD_ARGUMENTS));
                return false;
            }

            return playerPasswordService.createPassword(player, args[0], args[1]);
        }

        return true;
    }

    private boolean resetPassword(CommandSender commandSender, String[] args) {
        if (commandSender instanceof Player player) {
            return playerPasswordService.resetPassword(player);
        }

        return true;
    }

    private boolean login(CommandSender commandSender, String[] args) {
        if (commandSender instanceof Player player) {

            if (invalidArguments(args, 1)) {
                commandSender.sendMessage(ChatColor.RED + get(INVALID_LOGIN_ARGUMENTS));
                return false;
            }

            return playerLoginService.login(player, args[0]);
        }

        return true;
    }

    private boolean invalidArguments(String[] args, int argsExpected) {
        return !(args.length == argsExpected);
    }

}
