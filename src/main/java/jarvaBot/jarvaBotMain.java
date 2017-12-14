package jarvaBot;


import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.HierarchyException;
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;



public class jarvaBotMain extends ListenerAdapter {
    public static void main(String[] args) {

        String loginToken = "";
        try(BufferedReader br = new BufferedReader(new FileReader("src/token.txt"))){
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line !=null){
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            loginToken = sb.toString().trim();


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(loginToken);

        try{
            JDA jda = new JDABuilder(AccountType.BOT).setToken(loginToken).addEventListener(new jarvaBotMain()).buildBlocking();
        } catch(LoginException e){
            e.printStackTrace();
        } catch(InterruptedException e){
            e.printStackTrace();
        } catch(RateLimitedException e){
            e.printStackTrace(); }



    }

    /*
    TODO: Classify, Prettify
     */
    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
       if (event.getAuthor().isBot()) return; // don't bother with other bots

        JDA jda = event.getJDA();
        long responseNumber = event.getResponseNumber();

        Guild guild = event.getGuild();

        User author = event.getAuthor();
        Message message = event.getMessage();
        MessageChannel channel = event.getChannel();
        Member selfMember = guild.getSelfMember();
        Member user = event.getMember();


        String msg = message.getContent();
        String[] msgSplit = msg.split(" (?=(([^'\"]*['\"]){2})*[^'\"]*$)"); // split spaces but not within double quotes
        // getRawContent() is an atomic getter
        // getContent() is a getter which strips discord's formating


        if(event.isFromType(ChannelType.TEXT)){
            TextChannel textChannel = event.getTextChannel();
            Member member = event.getMember();

            String name;
            // If it's a webhook message, there's no member, so we default to the author. Otherwise we will grab their nick.
            if(message.isWebhookMessage()){
                name = author.getName();
            }
            else{
                name = member.getEffectiveName();
            }
            System.out.printf("(%s)[%s]<%s>: %s\n", guild.getName(), textChannel.getName(), name, msg);
        }

        else if (event.isFromType(ChannelType.PRIVATE)){
            PrivateChannel privateChannel = event.getPrivateChannel();

            System.out.printf("[PRIV]<%s>: %s\n", author.getName(), msg);
        }

        if(msg.toLowerCase().startsWith("\\help")){
            channel.sendMessage("I'm here to help! I'm Jarva, the javalicious bot made by an incompetent troll. \n" +
                    "All commands prefaced with a backslash \\ . Hopefully this will avoid any conflicts with other bots \n" +
                    "All current commands are as follows: \\role \\help \n" +
                    "Commands can take the word help for as a keyword to see how they function. Such as \\role help . This will display role's help message").queue();
        }

        if(msg.toLowerCase().startsWith("\\role")){

            ArrayList<Role> usersRoles = new ArrayList<Role>();
            for (Role role : user.getGuild().getRoles()){
                usersRoles.add(role);
                //System.out.println(role.getName());
            }

            int tokenAmount = msgSplit.length;

            if (tokenAmount == 1) {
                channel.sendMessage("Not enough arguments! \n Second argument should be the operation, the third the role, and the fourth the person to be applied to, if applicable.").queue();
                return;
            }

            if (tokenAmount > 4) {
                channel.sendMessage("Too many arguments! There should be a max of two after \\role ! \n Make sure to encapsulate the role name in double quotes \" like this \" ").queue();
                return;

            } //remove this when the color command is added


            String roleName = "";
            if (tokenAmount > 2){
                roleName = msgSplit[2];
                roleName = roleName.replace("\"", "").trim();
            }

            if (msgSplit[1].equalsIgnoreCase("help")){
                channel.sendMessage("\\role is a command where you can make(makes a role for the server), add(adds a role to yourself), delete(deletes a role from the server), and remove(removes a role from yourself) roles by name. \n" +
                        "                   The expected syntax is \\role keyword \"role name\" such as \\role add \"Blue\" ").queue();
            }
            else if (msgSplit[1].equalsIgnoreCase("make")){
                channel.sendMessage("m").queue();
            }
            else if(msgSplit[1].equalsIgnoreCase("add")){
                if (tokenAmount < 3) {
                    channel.sendMessage("Too few arguments!").queue();
                    return;
                } else if (tokenAmount > 3) {
                    channel.sendMessage("Too many arguments!").queue();
                    return;
                }
                System.out.println(roleName);
                for (Role role : user.getGuild().getRoles()){
                    System.out.println(role.getName());

                    if (roleName.equalsIgnoreCase(role.getName().trim())) {
                        System.out.println(roleName);
                        try{
                            user.getGuild().getController().addSingleRoleToMember(user,role).queue();
                        }
                        catch (InsufficientPermissionException e){
                            channel.sendMessage("Can't give the role! Insufficient permissions! Make sure I can actually manage roles.").queue();
                            e.printStackTrace();
                        }
                        catch (HierarchyException e){
                            channel.sendMessage("Your permissions are higher in the role hierarchy than mine, which means I can't change your roles!").queue();
                            e.printStackTrace();
                        }


                    }

                }
            }
            else if (msgSplit[1].equalsIgnoreCase("delete")){
                channel.sendMessage("d").queue();
            }
            else if (msgSplit[1].equalsIgnoreCase("remove")){
                channel.sendMessage("r").queue();
            }
            else{
                channel.sendMessage("Invalid first argument! Only use make, add, delete, remove, or color!").queue();
            }
            /*
            TODO: commands to add to role: make, delete, remove, color
            TODO: add \server which will scrape an internal database of all the servers we host
             */
        }
    }
}
