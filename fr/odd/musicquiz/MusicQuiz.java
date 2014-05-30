package fr.odd.musicquiz;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import com.xxmicloxx.NoteBlockAPI.NBSDecoder;
import com.xxmicloxx.NoteBlockAPI.RadioSongPlayer;
import com.xxmicloxx.NoteBlockAPI.Song;
import com.xxmicloxx.NoteBlockAPI.SongPlayer;


public class MusicQuiz extends JavaPlugin implements Listener
{
	
	Scoreboard board;
	Scoreboard board2;
	Scoreboard board3;
	
	int playersNumber;
	int playersMax = 16;
	int deco;	
	int secondes;
	int playersAutostart;
	
	boolean start = true;
	boolean cooldown = true;
	boolean forceStart = false;
	
	private ArrayList<String> nbs = new ArrayList<String>();
	private ArrayList<Player> players = new ArrayList<Player>();
	private String PREFIX = ChatColor.RED + "["  +ChatColor.YELLOW + "MusicQuiz" + ChatColor.RED + "] "+ChatColor.RESET ;

	final File fichierConfig = new File("/plugins/MusicQuiz/Musique.yml"); // Le fichier .yml
	final FileConfiguration config = YamlConfiguration.loadConfiguration(fichierConfig); 
	public void onEnable()
	{
		
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(this, this);	
		getConfig().options().copyDefaults(true);
		saveConfig();
		
		
	}



	@Override
    public boolean onCommand(final CommandSender sender, Command cmd, String commandLabel, String[] args) 
	{
		

		if(cmd.getName().equalsIgnoreCase("mquiz"))
		{
			ScoreboardManager manager = Bukkit.getScoreboardManager();
			board = manager.getNewScoreboard();
			final Objective objective = board.registerNewObjective("Test", "Test2");
			if(sender instanceof Player)
			{
				if(args.length < 1)
				{
					
						sender.sendMessage(ChatColor.RED+"Erreur:");
						sender.sendMessage(ChatColor.RED+" - /mquiz join");
						sender.sendMessage(ChatColor.RED+" - /mquiz start");
						sender.sendMessage(ChatColor.RED+" - /mquiz stop");
						sender.sendMessage(ChatColor.RED+" - /mquiz top");
						sender.sendMessage(ChatColor.RED+" - /mquiz leave");
						sender.sendMessage(ChatColor.RED+" - /mquiz setminplayers");
						sender.sendMessage(ChatColor.RED+" - /mquiz setstart");
						sender.sendMessage(ChatColor.RED+" - /mquiz addmusic");
						
				}
				else
				{
				if(args[0].equalsIgnoreCase("join"))
				{
					
					if((players.contains((Player)sender)) && forceStart == true)
					{
						sender.sendMessage(PREFIX +ChatColor.RED+ "Erreur : tu es deja en partie");
						
					return false;}
					else
					{
						playersNumber++;
							
							//partie scoreboard
							
							objective.setDisplayName(ChatColor.GRAY + "Mquiz commence dans : " +ChatColor.GOLD+"--");
							objective.setDisplaySlot(DisplaySlot.SIDEBAR);
							
							//partie attribution des points au scoreboard
							
							Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() 
							{
								
								@SuppressWarnings("deprecation")
								@Override
								public void run() 
								{
									for(Player p : players)
									{
									Score score = objective.getScore(Bukkit.getOfflinePlayer(ChatColor.GOLD + "Joueurs :"));
									score.setScore(playersNumber);
									Score score1 = objective.getScore(Bukkit.getOfflinePlayer(ChatColor.GOLD + "Joueurs min :"));
									score1.setScore(getConfig().getInt("autostart_players"));
								}}
							}, 0, 10);
					
							//partie attribution de scoreboard au joueur
							((Player) sender).setScoreboard(board);
							players.add((Player) sender);
							
							playersAutostart = getConfig().getInt("autostart_players");
							
							if(start == false)
							{
								cooldown = false;
							}
							if(playersNumber >= 2)
							{
								forceStart = true;
							}
							else
							{
								forceStart = false;
							}
						}}
				
				if(args[0].equalsIgnoreCase("leave"))
				{
					if(players.contains((Player) sender))
					{
					playersNumber--;
					if(playersNumber < 2)
					{
						
						ScoreboardManager manager2 = Bukkit.getScoreboardManager();
						board2 = manager2.getNewScoreboard();
						final Objective objective2 = board.registerNewObjective("Null", "Null");
						for(Player p : players)
						{
							objective.setDisplayName("");
							objective.setDisplaySlot(DisplaySlot.SIDEBAR);
							p.setScoreboard(board2);
							p.sendMessage(PREFIX + ChatColor.RED+"La partie a été supprimée car il ne reste plus qu'un joueur.");
						}
						Bukkit.getScheduler().runTaskLater(this, new Runnable() 
						{
							
							@Override
							public void run() 
							{
								players.clear();
								playersNumber = 0;
								Bukkit.getScheduler().cancelAllTasks();
								
							}
						}, 10);
					}
					else
					{
						players.remove((Player) sender);
					}
				}else
				{
					sender.sendMessage(PREFIX+ChatColor.RED+"Erreur : Vous n'etes pas dans la partie.");
				}
				}
				if(args[0].equalsIgnoreCase("start")){
					if(start == true)
					{
					if(forceStart != true)
					{
						sender.sendMessage(PREFIX +ChatColor.RED+ "Erreur : Le nombre minimum de joueurs requis n'est pas encore atteint.");
					}
					else
					{
						secondes = getConfig().getInt("Cooldown");
						if(secondes < 5)
						{
							sender.sendMessage(PREFIX +ChatColor.RED+  "Erreur : Le cooldown n'a pas été défini : /mquiz setstart <numéro>");
						}else{
						start = false;
						for(Player p : players){	
						p.sendMessage(PREFIX +ChatColor.GREEN+  "La partie va commencer dans : "+getConfig().getInt("Cooldown")+" secondes.");
						}
						Bukkit.getScheduler().cancelAllTasks();
						secondes = getConfig().getInt("Cooldown");
							if(secondes < 5){
								Bukkit.broadcastMessage(PREFIX +ChatColor.RED+ "Erreur : Le cooldown qui est affiché dans le fichié de config est inféieur é 5.");
							}else{
							Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
								
								@Override
								public void run() {
										//ici scoreboard
									objective.setDisplayName(ChatColor.GRAY+"Mquiz commence dans : " +ChatColor.GOLD+ secondes+"s");
									secondes--;
									objective.setDisplaySlot(DisplaySlot.SIDEBAR);
									Score score = objective.getScore(Bukkit.getOfflinePlayer(ChatColor.GOLD + "Joueurs :"));
									score.setScore(playersNumber);
									for(Player p : players){
										p.setScoreboard(board);
									}
									if(secondes < 0)
									{
										Bukkit.getScheduler().cancelAllTasks();
										for(Player p : players)
										{
											p.sendMessage(PREFIX +ChatColor.GREEN+  "La partie vient de débuter! Bon jeu.");
											objective.setDisplayName(ChatColor.RED + "C'est parti !");
										}
									}
								}
							}, 0, 20);
						}
						}
					}}else{
						sender.sendMessage(PREFIX +ChatColor.RED+ "La partie est déjà lancée");
					}
				}
				if(args[0].equalsIgnoreCase("addmusic")){
					
					if(args.length == 2 && !args[1].endsWith(".nbs")){
							int num_musique;
							
							num_musique = getConfig().getInt("Numero_musique");
							
							num_musique++;
							getConfig().set("Numero_musique", num_musique);
							getConfig().set("Nom_musique", args[1]);
							saveConfig();
									getConfig().set(""+getConfig().getInt("Numero_musique"), getConfig().get("Nom_musique"));
									saveConfig();
									sender.sendMessage(PREFIX+ChatColor.GREEN+"La musique "+ChatColor.AQUA+getConfig().getString(""+getConfig().getInt("Numero_musique"))+".nbs"+ChatColor.GREEN+" a bien été ajoutée.");									
							
							
							
					}else{
						sender.sendMessage(PREFIX + ChatColor.RED+"Erreur : veuillez entre le nom d'une musique sans le .nbs et prenez soin de replacer les espaces dans le nom de la musique par des tirets _ .");
					}
				}
				if(args[0].equalsIgnoreCase("setstart")){
					if(start == true){
					
					if(args.length == 2){
					if(Integer.parseInt(args[1]) < 5 || Integer.parseInt(args[1]) > 59){
						
						sender.sendMessage(PREFIX +ChatColor.RED+ "Erreur : Le numéro entré n'est pas valide : "+args[1]+" < 5 ou > 59 secondes");
					}else{
						secondes = Integer.parseInt(args[1]);
						getConfig().set("Cooldown", secondes);
						saveConfig();
						sender.sendMessage(PREFIX +ChatColor.GREEN+ "Le cooldown a bien été changé à "+secondes+" secondes.");
						
					}}else{
						sender.sendMessage(PREFIX +ChatColor.RED+ "Erreur : Veuillez entrer un numéro supérieur é 4.");
					}
				}else{
					sender.sendMessage(PREFIX +ChatColor.RED+  "Erreur : Le cooldown ne peut pas étre modifié lorsque la partie é déjé débuté.");}
				}
				
				
				if(args[0].equalsIgnoreCase("setminplayers")){
					if(args.length == 2){
						if(Integer.parseInt(args[1]) < 2 || Integer.parseInt(args[1]) > 200){
							sender.sendMessage(PREFIX +ChatColor.RED+  "Erreur : Le numéro entré n'est pas valide : "+args[1]+" < 2 ou > 200 joueurs");
						}else{
							playersAutostart = Integer.parseInt(args[1]);
							getConfig().set("autostart_players", playersAutostart);
							saveConfig();
							sender.sendMessage(PREFIX +ChatColor.GREEN+  "Le nombre minimum de joueurs a bien été changé à "+playersAutostart+" joueurs.");
						}}else{
							sender.sendMessage(PREFIX +ChatColor.RED+  "Erreur : Veuillez entrer un numéro sup&rieur à 1.");
						}
					
				}
				
				if(args[0].equalsIgnoreCase("leave"))
				{
					players.remove((Player)sender);
					
				}
			}
		}}
		
		
		return false;
		

}
	@EventHandler(priority = EventPriority.NORMAL)
	public void onLeave(PlayerQuitEvent e)
	{
		if(players.contains((Player) e.getPlayer()))
		{
			e.setQuitMessage(ChatColor.YELLOW+ PREFIX +""+e.getPlayer().getName()+" a quitté le serveur et la partie MQuizz.");
			playersNumber--;
			players.remove(e.getPlayer());

		}
		else
		{
					e.setQuitMessage(ChatColor.YELLOW+""+e.getPlayer().getName()+" left the game.");
		}
	}
	public void playMusic(String string)
	 {
	  Song s = NBSDecoder.parse(new File(getDataFolder(), string));
	  SongPlayer sp = new RadioSongPlayer(s);
	  sp.setAutoDestroy(true);
	  for(Player p : players)
	  {
	   sp.addPlayer(p);
		  sp.setPlaying(true);
	  }
	  
	 }	
	
	
	}
		

		

	