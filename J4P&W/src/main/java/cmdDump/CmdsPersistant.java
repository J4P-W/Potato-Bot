package cmdDump;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

import org.javacord.api.entity.channel.Channel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import DevPaw.api.classes.Nation;
import DevPaw.api.classes.War.WarData;
import DevPaw.api.classes.Wars.LWar;
import DevPaw.api.exceptions.UnsuccessfullAPIException;
import DevPaw.api.listeners.DevListener;
import main.App;
import main.Init;

public class CmdsPersistant {
	protected static Map<String, Timer> timers = new HashMap<>();

	private CmdsPersistant() {
		super();
	}

	public static void marketTracker(Message m) {
		m.getChannel().sendMessage("Tracking...");
		if (!timers.containsKey(m.getChannel().getIdAsString())) {
			Timer t = new Timer();
			t.schedule(new TimerTask() {
				public void run() {
					Cmds3.market(m);
				}
			}, 0, 60000);
			timers.put(m.getChannel().getIdAsString(), t);
		} else
			m.getChannel().sendMessage("There is already a market stream for this channel");
	}

	public static void stopTracker(Message m) {
		Timer t = timers.remove(m.getChannel().getIdAsString());
		if (t == null)
			m.getChannel().sendMessage("There was no market stream allocated to this channel!");
		else {
			t.cancel();
			t.purge();
			m.getChannel().sendMessage("Market Stream was disabled.");
		}
	}

	public static void removeDefensive(Message m) throws IOException {
		if (Init.warExecutor.listeners.containsKey(m.getChannel().getIdAsString())) {
			Init.warExecutor.listeners.remove(m.getChannel().getIdAsString());
			Init.warExecutor.listendb.save(Init.warExecutor.listeners);
			m.getChannel().sendMessage("Removed!");
		} else {
			m.getChannel().sendMessage("No defender found for this channel for that id");
		}
	}

	public static void removeDefensive(String channelid) throws IOException {
		if (Init.warExecutor.listeners.containsKey(channelid)) {
			Init.warExecutor.listeners.remove(channelid);
			Init.warExecutor.listendb.save(Init.warExecutor.listeners);
		}
	}

	public static void defensiveWarTracker(Message m) throws IOException {
		TextChannel c = m.getChannel();
		String[] args = m.getContent().split(" ");
		if (!args[1].equalsIgnoreCase("all")) {
			try {
				App.mainapi.getAlliance(args[1]);
			} catch (UnsuccessfullAPIException error) {
				ErrorResponses.APIException(c, error);
				return;
			} catch (ArrayIndexOutOfBoundsException error) {
				ErrorResponses.invalidFormatException(c, error);
				return;
			}
		}

		Init.warExecutor.listeners.put(m.getChannel().getIdAsString(), new DevListener<LWar>() {
			private static final long serialVersionUID = -882489753234470874L;
			public String allianceid = m.getContent().split(" ")[1];
			public String channelid = c.getIdAsString();

			@Override
			public void execute(List<LWar> data) {
				if (data.isEmpty())
					System.out.println("New War Detected!");
				try {
					for (int x = 0; x < data.size(); x++)
						if (App.mainapi.getNation(data.get(x).defenderID).allianceid.contentEquals(allianceid)
								|| allianceid.equalsIgnoreCase("all")) {
							System.out.println("Sucess!");
							EmbedBuilder embed = new EmbedBuilder();
							WarData w = App.mainapi.getWarData(data.get(x).warID);
							embed.setTitle("New War Declaration!");
							embed.setUrl("https://politicsandwar.com/nation/war/timeline/war=" + data.get(x).warID);
							Nation agro = App.mainapi.getNation(w.aggressorId);
							Nation defe = App.mainapi.getNation(w.defenderId);
							embed.addInlineField("Invader's nation", CmdsUtil.hyperUrl(agro.name + "-",
									"https://politicsandwar.com/nation/id=" + agro.nationid));
							embed.addInlineField("Invader's alliance", CmdsUtil.hyperUrl(agro.alliance + "-",
									"https://politicsandwar.com/alliance/id=" + agro.allianceid));
							embed.addInlineField("Invader's score", agro.score + "#");
							embed.addInlineField("Defender's nation", CmdsUtil.hyperUrl(defe.name + "-",
									"https://politicsandwar.com/nation/id=" + defe.nationid));
							embed.addInlineField("Defender's alliance", CmdsUtil.hyperUrl(defe.alliance + "-",
									"https://politicsandwar.com/alliance/id=" + defe.allianceid));
							embed.addInlineField("Defender's score", defe.score + "#");
							embed.addInlineField("Invader's soldiers", agro.soldiers + " -");
							embed.addInlineField("Defender's soldiers", defe.soldiers + " -");
							embed.addInlineField("Advantage",
									(Integer.parseInt(agro.soldiers) <= Integer.parseInt(defe.soldiers))
											? ":white_check_mark: "
											: ":x: ");
							embed.addInlineField("Invader's tanks", agro.tanks + " -");
							embed.addInlineField("Defender's tanks", defe.tanks + " -");
							embed.addInlineField("Advantage",
									(Integer.parseInt(agro.tanks) <= Integer.parseInt(defe.tanks))
											? ":white_check_mark: "
											: ":x: ");
							embed.addInlineField("Invader's planes", agro.aircraft + " -");
							embed.addInlineField("Defender's planes", defe.aircraft + " -");
							embed.addInlineField("Advantage",
									(Integer.parseInt(agro.aircraft) <= Integer.parseInt(defe.aircraft))
											? ":white_check_mark: "
											: ":x: ");
							embed.addInlineField("Invader's ships", agro.ships + " -");
							embed.addInlineField("Defender's ships", defe.ships + " -");
							embed.addInlineField("Advantage",
									(Integer.parseInt(agro.ships) <= Integer.parseInt(defe.ships))
											? ":white_check_mark: "
											: ":x: ");
							embed.addInlineField("Invader's missiles", agro.missiles + " -");
							embed.addInlineField("Defender's missiles", defe.missiles + " -");
							embed.addInlineField("Advantage",
									(Integer.parseInt(agro.missiles) <= Integer.parseInt(defe.missiles))
											? ":white_check_mark: "
											: ":x: ");
							embed.addInlineField("Invader's nukes", agro.nukes + " -");
							embed.addInlineField("Defender's nukes", defe.nukes + " -");
							embed.addInlineField("Advantage",
									(Integer.parseInt(agro.nukes) <= Integer.parseInt(defe.nukes))
											? ":white_check_mark: "
											: ":x: ");
							embed.setFooter("War monitor system made by Devan S.",
									"https://cdn.discordapp.com/avatars/358794817595113476/48192cb27b43069e544052b814c48df1.png?size=256");
							Optional<Channel> channel = App.api.getChannelById(channelid);
							if (channel.isPresent())
								channel.get().asServerTextChannel().get().sendMessage(embed);
							else
								removeDefensive(channelid);
						}
				} catch (UnsuccessfullAPIException | IOException e) {
					e.printStackTrace();
				}
			}
		});
		c.sendMessage("Tracking will start now!");
		Init.warExecutor.listendb.save(Init.warExecutor.listeners);
	}

}
