package com.ranull.graves.integration;

import com.ranull.graves.Graves;
import com.xxmicloxx.NoteBlockAPI.model.Song;
import com.xxmicloxx.NoteBlockAPI.songplayer.RadioSongPlayer;
import com.xxmicloxx.NoteBlockAPI.songplayer.SongPlayer;
import com.xxmicloxx.NoteBlockAPI.utils.NBSDecoder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to handle playing and stopping NoteBlockAPI songs for individual players or all players on the server.
 */
public class NoteBlockAPI {

    private final Graves plugin;
    private final Map<Object, SongPlayer> activeSongPlayers = new HashMap<>();

    /**
     * Constructs a new NoteBlockAPI instance.
     *
     * @param plugin the Graves plugin instance.
     */
    public NoteBlockAPI(Graves plugin) {
        this.plugin = plugin;
    }

    /**
     * Plays an NBS song file for a specified player.
     *
     * @param player   the player to play the song for.
     * @param fileName the name of the NBS file located in the /nbs directory.
     */
    public void playSongForPlayer(Player player, String fileName) {
        stopSongForPlayer(player);

        File songFile = new File(plugin.getDataFolder(), "nbs/" + fileName + ".nbs");
        if (!songFile.exists()) {
            player.sendMessage("The specified song file does not exist.");
            return;
        }

        Song song = NBSDecoder.parse(songFile);
        if (song == null) {
            player.sendMessage("Failed to load the song file.");
            return;
        }

        SongPlayer songPlayer = new RadioSongPlayer(song);
        songPlayer.setTick((short) (20 / song.getSpeed()));
        songPlayer.addPlayer(player);
        songPlayer.setPlaying(true);
        activeSongPlayers.put(player, songPlayer);
    }

    /**
     * Plays an NBS song file for all online players on the server.
     *
     * @param fileName the name of the NBS file located in the /nbs directory.
     */
    public void playSongForAllPlayers(String fileName) {
        stopSongForAllPlayers();

        File songFile = new File(plugin.getDataFolder(), "nbs/" + fileName + ".nbs");
        if (!songFile.exists()) {
            Bukkit.getLogger().warning("The specified song file does not exist.");
            return;
        }

        Song song = NBSDecoder.parse(songFile);
        if (song == null) {
            Bukkit.getLogger().warning("Failed to load the song file.");
            return;
        }

        SongPlayer songPlayer = new RadioSongPlayer(song);
        songPlayer.setTick((short) (20 / song.getSpeed()));
        for (Player player : Bukkit.getOnlinePlayers()) {
            songPlayer.addPlayer(player);
        }
        songPlayer.setPlaying(true);
        activeSongPlayers.put("allPlayers", songPlayer);
    }

    /**
     * Stops the currently playing song for a specific player.
     *
     * @param player the player for whom to stop the song.
     */
    public void stopSongForPlayer(Player player) {
        SongPlayer songPlayer = activeSongPlayers.remove(player);
        if (songPlayer != null) {
            songPlayer.setPlaying(false);
            songPlayer.destroy();
        }
    }

    /**
     * Stops the currently playing song for all players.
     */
    public void stopSongForAllPlayers() {
        SongPlayer songPlayer = activeSongPlayers.remove("allPlayers");
        if (songPlayer != null) {
            songPlayer.setPlaying(false);
            songPlayer.destroy();
        }
    }

    /**
     * Stops all active song players.
     */
    public void stopAllSongs() {
        for (SongPlayer songPlayer : activeSongPlayers.values()) {
            songPlayer.setPlaying(false);
            songPlayer.destroy();
        }
        activeSongPlayers.clear();
    }

    /**
     * Checks if a song is currently playing for a specific player.
     *
     * @param player the player to check for active song playback.
     * @return true if a song is actively playing for the player, false otherwise.
     */
    public boolean isSongPlayingForPlayer(Player player) {
        SongPlayer songPlayer = activeSongPlayers.get(player);
        return songPlayer != null && songPlayer.isPlaying();
    }

    /**
     * Checks if a song is currently playing for all players.
     *
     * @return true if a song is actively playing for all players, false otherwise.
     */
    public boolean isSongPlayingForAllPlayers() {
        SongPlayer songPlayer = activeSongPlayers.get("allPlayers");
        return songPlayer != null && songPlayer.isPlaying();
    }
}