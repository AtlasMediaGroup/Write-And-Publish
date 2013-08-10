package me.RyanWild.writeandpublish;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

public class writeandpublish extends JavaPlugin
{

    public static final Logger logger = Logger.getLogger("Minecraft-Server");
    private final writeandpublishbookwriter writer = new writeandpublishbookwriter(this);
    public static Economy econ = null;

    @Override
    public void onEnable()
    {
        try
        {
            Metrics metrics = new Metrics(this);
            metrics.start();
        }
        catch (IOException e)
        {
            // Failed to submit the stats :-(
        }

        if (!setupEconomy())
        {
            writeandpublish.logger.severe(String.format("[Write & Publish] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        else
        {
            writeandpublish.logger.info("[Write & Publish] Write & Publish is hooked with Vault, economic functions are enabled.");
        }

        File configFile = new File(getDataFolder().getPath() + File.separatorChar + "config.yml");
        if (!configFile.exists())
        {
            writeandpublish.logger.info("[Write & Publish] There was no config.yml found, it will be created now.");
            saveDefaultConfig();
        }

        File exampleFile = new File(getDataFolder().getPath() + File.separatorChar + "books" + File.separatorChar + "example");
        if (!exampleFile.exists())
        {
            createExample(exampleFile, "example.txt");
        }
        writeandpublish.logger.info("[Write & Publish] Write & Publish is enabled!");
        writeandpublish.logger.info("[Write & Publish] [info] Write & Publish is owned and maintained by Wild1145 and was created by Diederikmc.");
        writeandpublish.logger.info("[Write & Publish] [info] Thanks to Blindw4lk3r for the code to write books to files and convert them back");
    }

    private void createExample(File file, String source)
    {
        new File(getDataFolder().getPath() + File.separatorChar + "books").mkdir();
        try
        {
            file.createNewFile();
        }
        catch (IOException e)
        {
        }

        InputStream is = getClass().getResourceAsStream("/" + source);
        BufferedInputStream buffIn = new BufferedInputStream(is);

        BufferedOutputStream bufOut = null;
        try
        {
            bufOut = new BufferedOutputStream(new FileOutputStream(file));
        }
        catch (FileNotFoundException e1)
        {
        }

        byte[] inByte = new byte[4096];
        int count = -1;
        try
        {
            while ((count = buffIn.read(inByte)) != -1)
            {
                bufOut.write(inByte, 0, count);
            }

        }
        catch (IOException e)
        {
        }

        try
        {
            bufOut.close();
        }
        catch (IOException e)
        {
        }
        try
        {
            buffIn.close();
        }
        catch (IOException e)
        {
        }
    }

    @Override
    public void onDisable()
    {
        writeandpublish.logger.info("[Write & Publish] Write & Publish is disabled.");
    }

    public writeandpublishbookwriter getWriter()
    {
        return this.writer;
    }

    private boolean setupEconomy()
    {
        if (getServer().getPluginManager().getPlugin("Vault") == null)
        {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null)
        {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (sender instanceof Player)
        {
            Player player = (Player) sender;
            if (cmd.getName().equalsIgnoreCase("listbooks"))
            {
                if (player.hasPermission("WandP.listbooks"))
                {
                    player.sendMessage(ChatColor.BLUE + "[Write & Publish] Published books: " + ChatColor.YELLOW + getStringList(listBooks()));
                    return true;
                }
                else
                {
                    player.sendMessage(ChatColor.RED + "[Write & Publish] You do not have permission to do that.");
                    return false;
                }
            }
            if (cmd.getName().equalsIgnoreCase("publish"))
            {
                int publishprice = getConfig().getInt("publishprice");

                if (!player.hasPermission("WandP.publish"))
                {
                    player.sendMessage(ChatColor.RED + "[Write & Publish] You do not have permission to publish any book.");
                    return false;
                }
                if (args.length < 1)
                {
                    sender.sendMessage(ChatColor.RED + "[Write & Publish] No name specified for the book, please try again with a specified name.");
                    return false;
                }
                if (args.length > 1)
                {
                    sender.sendMessage(ChatColor.RED + "[Write & Publish] Multiple names are specified for the book, please try again with ONE specified name.");
                    return false;
                }
                String book = args[0];
                if (bookExists(book))
                {
                    player.sendMessage(ChatColor.RED + "[Write & Publish] You used an already existing name, try again with an other name.");
                    return false;
                }
                if (args.length == 1)
                {

                    EconomyResponse r = econ.withdrawPlayer(player.getName(), publishprice);
                    if (r.transactionSuccess())
                    {
                        player.sendMessage(ChatColor.BLUE + "[Write & Publish] You published the book succesfully for " + ChatColor.YELLOW + econ.format(publishprice) + " " + econ.currencyNamePlural() + ChatColor.BLUE + ".");
                        getWriter().saveBook(player, args[0]);
                        return true;
                    }
                    else
                    {
                        sender.sendMessage(String.format("An error occured: ", r.errorMessage));
                        return false;
                    }
                }
                else
                {
                    player.sendMessage(ChatColor.RED + "[Write & Publish] An error occured while saving the book.");
                    return false;
                }
            }
            if (cmd.getName().equalsIgnoreCase("buybook"))
            {
                int buyprice = getConfig().getInt("buyprice");
                if (args.length < 1)
                {
                    player.sendMessage(ChatColor.RED + "[Write & Publish] no name is specified for the book, please try again with a specified name.");
                    return false;
                }
                String book = args[0];
                if (bookExists(book))
                {
                    if (args.length > 30)
                    {
                        player.sendMessage(ChatColor.RED + "[Write & Publish] You used more than 30 args, WOW! ~easter egg~");
                        return false;
                    }
                    if (args.length > 2)
                    {
                        player.sendMessage(ChatColor.RED + "[Write & Publish] Multiple names are specified for the book or optional player, please try again with ONE specified name.");
                        return false;
                    }

                    File bookbook = new File(getDataFolder().getPath() + File.separatorChar + "books" + File.separatorChar + args[0]);
                    if (args.length == 1)
                    {
                        if (!player.hasPermission("WandP.buybook.foryourself"))
                        {
                            player.sendMessage(ChatColor.RED + "[Write & Publish] You do not have permission to do that.");
                            return false;
                        }
                        else
                        {

                            EconomyResponse r = econ.withdrawPlayer(player.getName(), buyprice);
                            if (r.transactionSuccess())
                            {
                                paytheauthor(bookbook, args[0], player.getName());
                                player.sendMessage(ChatColor.BLUE + "[Write & Publish] You bought the book: " + ChatColor.YELLOW + args[0] + ChatColor.BLUE + " for " + ChatColor.YELLOW + econ.format(buyprice) + " " + econ.currencyNamePlural() + ChatColor.BLUE + ".");
                                getWriter().giveBook(player, args[0]);
                                return true;
                            }
                            else
                            {
                                sender.sendMessage(String.format("An error occured: ", r.errorMessage));
                                return false;
                            }
                        }
                    }
                    if (args.length == 2)
                    {
                        if (!player.hasPermission("WandP.buybook.other"))
                        {
                            player.sendMessage(ChatColor.RED + "[Write & Publish] You do not have permission to do that.");
                            return false;
                        }
                        Player pla = getServer().getPlayer(args[1]);
                        if (pla == null)
                        {
                            player.sendMessage(ChatColor.RED + "[Write & Publish] There is no Player with that name " + ChatColor.YELLOW + args[1] + ChatColor.RED + " online.");
                            return false;
                        }
                        else
                        {

                            EconomyResponse r = econ.withdrawPlayer(player.getName(), buyprice);
                            if (r.transactionSuccess())
                            {
                                paytheauthor(bookbook, args[0], player.getName());
                                player.sendMessage(ChatColor.BLUE + "[Write & Publish] You bought the book: " + ChatColor.YELLOW + args[0] + ChatColor.BLUE + " for " + ChatColor.YELLOW + econ.format(buyprice) + " " + econ.currencyNamePlural() + ChatColor.BLUE + " for the player " + ChatColor.YELLOW + pla.getName() + ChatColor.BLUE + ".");
                                pla.sendMessage(ChatColor.BLUE + "[Write & Publish] You received the book: " + ChatColor.YELLOW + args[0] + ChatColor.BLUE + " from the player " + ChatColor.YELLOW + player.getName() + ChatColor.BLUE + ".");
                                getWriter().giveBook(pla, args[0]);
                                return true;
                            }
                            else
                            {
                                sender.sendMessage(String.format("An error occured: ", r.errorMessage));
                                return false;
                            }
                        }
                    }
                    else
                    {
                        player.sendMessage(ChatColor.RED + "[Write & Publish] No name specified for the book, please try again with a specified name.");
                        return false;
                    }
                }
                else
                {
                    player.sendMessage(ChatColor.RED + "[Write & Publish] There is no book with the name: '" + ChatColor.YELLOW + book + ChatColor.RED + "' published!");
                    return false;
                }
            }
        }
        else
        {
            sender.sendMessage(ChatColor.RED + "[Write & Publish] You must be a player to buy books!");
            return false;
        }
        return false;
    }

    private String getStringList(String[] listBooks)
    {
        String list = "";

        for (String i : listBooks)
        {
            list = list + i + ", ";
        }

        return list;
    }

    private boolean bookExists(String buch)
    {
        String[] list = listBooks();
        for (String s : list)
        {
            if (s.equals(buch))
            {
                return true;
            }
        }
        return false;
    }

    private String[] listBooks()
    {
        File folder = new File(getDataFolder().getPath() + File.separatorChar + "books");
        File[] files;
        files = folder.listFiles();
        String[] books = new String[files.length];
        for (int i = 0; i < files.length; i++)
        {
            books[i] = files[i].getName();
        }
        return books;
    }

    private void paytheauthor(File bookbook, String bookname, String buyer)
    {

        YamlConfiguration saved = new YamlConfiguration();
        try
        {

            saved.load(bookbook);
            String temp = saved.getString("author");
            temp = remove(temp);
            int price = getConfig().getInt("moneyforauthor");
            econ.depositPlayer(temp, price);
            Player p = Bukkit.getServer().getPlayer(temp);
            p.sendMessage(ChatColor.BLUE + "[Write & Publish] You got " + ChatColor.YELLOW + econ.format(price) + " " + econ.currencyNamePlural() + ChatColor.BLUE + " because " + ChatColor.YELLOW + buyer + ChatColor.BLUE + " bought your book: " + ChatColor.YELLOW + bookname + ChatColor.BLUE + "!");
        }
        catch (InvalidConfigurationException e)
        {
        }
        catch (FileNotFoundException e)
        {
        }
        catch (IOException e)
        {
        }

    }

    private String remove(String line)
    {
        String temp = line;
        String replaceAll = temp.replaceAll("'", "");
        String replaceAll1 = temp.replaceAll("\"", "");

        return temp;
    }
}