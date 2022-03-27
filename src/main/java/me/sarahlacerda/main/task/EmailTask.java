package me.sarahlacerda.main.task;

import javax.mail.MessagingException;

import me.sarahlacerda.main.service.EmailService;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class EmailTask extends BukkitRunnable {
    private final EmailService emailService;
    private final String messageTemplate;
    private final String subject;
    private final String recipientEmail;
    private final int code;
    private final Player player;

    public EmailTask(EmailService emailService, String messageTemplate, Player player, String subject, String recipientEmail, int code) {
        this.emailService = emailService;
        this.messageTemplate = messageTemplate;
        this.player = player;
        this.recipientEmail = recipientEmail;
        this.code = code;
        this.subject = subject;
    }

    public void run() {

        try {
            emailService.sendEmail(recipientEmail, subject, prepareMessage(messageTemplate, code));
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
