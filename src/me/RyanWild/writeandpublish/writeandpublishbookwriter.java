package me.RyanWild.writeandpublish;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class writeandpublishbookwriter
{
  private writeandpublish plugin;
  private HashMap<String, ChatColor> colors = new HashMap<>();

  public writeandpublishbookwriter(writeandpublish instance)
  {
    this.plugin = instance;
    this.colors.put("&0", ChatColor.BLACK);
    this.colors.put("&1", ChatColor.DARK_BLUE);
    this.colors.put("&2", ChatColor.DARK_GREEN);
    this.colors.put("&3", ChatColor.DARK_AQUA);
    this.colors.put("&4", ChatColor.DARK_RED);
    this.colors.put("&5", ChatColor.DARK_PURPLE);
    this.colors.put("&6", ChatColor.GOLD);
    this.colors.put("&7", ChatColor.GRAY);
    this.colors.put("&8", ChatColor.DARK_GRAY);
    this.colors.put("&9", ChatColor.BLUE);
    this.colors.put("&a", ChatColor.GREEN);
    this.colors.put("&b", ChatColor.AQUA);
    this.colors.put("&c", ChatColor.RED);
    this.colors.put("&d", ChatColor.LIGHT_PURPLE);
    this.colors.put("&e", ChatColor.YELLOW);
    this.colors.put("&f", ChatColor.WHITE);
  }

  public boolean saveBook(Player pl, String name)
  {
    File bookfolder = new File(this.plugin.getDataFolder().getPath() + File.separatorChar + "books");
    if (!bookfolder.exists())
    {
      bookfolder.mkdirs();
    }

    File actBook = new File(this.plugin.getDataFolder().getPath() + File.separatorChar + "books" + File.separatorChar + name);

    if (pl.getItemInHand().getType() != Material.WRITTEN_BOOK)
    {
      pl.sendMessage(ChatColor.RED + "[Write & Publish] You're not holding a written book right now.");
      return false;
    }
    if (actBook.exists())
    {
      File[] books = bookfolder.listFiles();
      ArrayList<String> names = new ArrayList<>();
      for (File f : books)
      {
        names.add(f.getName());
      }
      if (names.contains(name))
      {
        pl.sendMessage(ChatColor.RED + "[Write & Publish] There is already a book with this name.");
      }
      return false;
    }
    try {
      actBook.createNewFile();
    } catch (IOException e) {
    }

    BookMeta bm = (BookMeta)pl.getItemInHand().getItemMeta();
    String author = bm.getAuthor();
    String title = bm.getTitle();
    List<String> pages = bm.getPages();
    writeBookToSystem(title, author, pages, actBook);

    return true;
  }


  public boolean giveBook(Player player1, String name)
  {
    ItemStack b = new ItemStack(Material.WRITTEN_BOOK);
    BookMeta book = (BookMeta)b.getItemMeta();
    File rb = new File(this.plugin.getDataFolder().getPath() + File.separatorChar + "books" + File.separatorChar + name);
    book = readFromSystem(book, rb);
    b.setItemMeta(book);
    player1.getInventory().addItem(new ItemStack[] { b });
    return true;
  }

  private void writeBookToSystem(final String t, final String a, final List<String> pages, final File b)
  {
    this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable()
    {
      @Override
      public void run() {
        YamlConfiguration bookfile = new YamlConfiguration();
        bookfile.set("author", a);
        bookfile.set("title", t);
        for (int i = 1; i <= pages.size(); i++)
        {
          String temp = writeandpublishbookwriter.this.replaceFormatChar(pages.get(i - 1));
          bookfile.set("page" + i, temp);
        }
        try {
          bookfile.save(b);
        } catch (IOException e) {
        }
      }
    });
  }

  private BookMeta readFromSystem(BookMeta book, File rb)
  {
    List<String> lines = new ArrayList<>();
    YamlConfiguration saved = new YamlConfiguration();
    try
    {
      saved.load(rb);
      String temp = saved.getString("author");
      temp = remove(temp);
      book.setAuthor(temp);

      temp = saved.getString("title");
      temp = remove(temp);
      book.setTitle(temp);
      for (int i = 1; i < 1000; i++)
      {
        temp = "";
        temp = saved.getString("page" + i);
        if (temp == null)
        {
          break;
        }
        temp = remove(temp);
        temp = getFormatChar(temp);
        lines.add(temp);
      }
    }
    catch (InvalidConfigurationException e)
    {
    }
    catch (FileNotFoundException e) {
    }
    catch (IOException e) {
    }
    String[] pages = new String[lines.size()];
    for (int i = 0; i < lines.size(); i++)
    {
      pages[i] = lines.get(i);
      if (lines.get(i).contains("&0"))
      {
        lines.set(i, setColor(lines.get(i), "&0"));
      }
      if (lines.get(i).contains("&1"))
      {
        lines.set(i, setColor(lines.get(i), "&1"));
      }
      if (lines.get(i).contains("&2"))
      {
        lines.set(i, setColor(lines.get(i), "&2"));
      }
      if (lines.get(i).contains("&3"))
      {
        lines.set(i, setColor(lines.get(i), "&3"));
      }
      if (lines.get(i).contains("&4"))
      {
        lines.set(i, setColor(lines.get(i), "&4"));
      }
      if (lines.get(i).contains("&5"))
      {
        lines.set(i, setColor(lines.get(i), "&5"));
      }
      if (lines.get(i).contains("&6"))
      {
        lines.set(i, setColor(lines.get(i), "&6"));
      }
      if (lines.get(i).contains("&7"))
      {
        lines.set(i, setColor(lines.get(i), "&7"));
      }
      if (lines.get(i).contains("&8"))
      {
        lines.set(i, setColor(lines.get(i), "&8"));
      }
      if (lines.get(i).contains("&9"))
      {
        lines.set(i, setColor(lines.get(i), "&9"));
      }
      if (lines.get(i).contains("&a"))
      {
        lines.set(i, setColor(lines.get(i), "&a"));
      }
      if (lines.get(i).contains("&b"))
      {
        lines.set(i, setColor(lines.get(i), "&b"));
      }
      if (lines.get(i).contains("&c"))
      {
        lines.set(i, setColor(lines.get(i), "&c"));
      }
      if (lines.get(i).contains("&d"))
      {
        lines.set(i, setColor(lines.get(i), "&d"));
      }
      if (lines.get(i).contains("&e"))
      {
        lines.set(i, setColor(lines.get(i), "&e"));
      }
      if (lines.get(i).contains("&f"))
      {
        lines.set(i, setColor(lines.get(i), "&f"));
      }

    }

    book.setPages(lines);
    return book;
  }

  private String setColor(String p, String c)
  {
    String temp = "";

    String[] result = p.split(c);
    for (int i = 0; i < result.length; i++)
    {
      temp = temp + result[i];
      if (i < result.length - 1)
      {
        temp = temp + this.colors.get(c);
      }
    }

    return temp;
  }

  private String remove(String line)
  {
    String temp = line;
      String replaceAll = temp.replaceAll("'", "");
      String replaceAll1 = temp.replaceAll("\"", "");

    return temp;
  }

  private String replaceFormatChar(String t)
  {
    for (int i = 0; i < t.length(); i++)
    {
      if (t.codePointAt(i) == 167)
      {
        String temp = t.substring(0, i) + ";;" + t.substring(i + 1);
        t = temp;
      }
    }
    return t;
  }

  private String getFormatChar(String t)
  {
    t = t.replaceAll(";;", "�");
    return t;
  }
}