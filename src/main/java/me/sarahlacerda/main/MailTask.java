package me.sarahlacerda.main;

import javax.mail.MessagingException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class MailTask extends BukkitRunnable {
    private final MailService mailService;
    private final String messageTemplate;
    private final String subject;
    private final String recipientEmail;
    private final int code;
    private final Player player;

    public MailTask(MailService mailService, String messageTemplate, Player player, String subject, String recipientEmail, int code) {
        this.mailService = mailService;
        this.messageTemplate = messageTemplate;
        this.player = player;
        this.recipientEmail = recipientEmail;
        this.code = code;
        this.subject = subject;
    }

    public void run() {

        try {
            mailService.sendEmail(recipientEmail, subject, prepareMessage(messageTemplate, code));

            player.sendMessage(ChatColor.GREEN + "A message with your code has been e-mailed to: " + recipientEmail);
            player.sendMessage(ChatColor.GREEN + "Once you receive your code type /code [code] to authenticate");

            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + player.getDisplayName() + "Requested code:" + code + " be sent to " + recipientEmail);
            this.cancel();
        } catch (MessagingException e) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Failed to send mail to: " + recipientEmail + e);
            player.sendMessage(ChatColor.RED + "Failed to send mail to: " + recipientEmail + "\nPlease Contact the Administrator.");
            this.cancel();
        }


    }

    private String prepareMessage(String messageTemplate, int code) {
        return messageTemplate.replaceAll("%CODE%", String.valueOf(code));
    }

}
