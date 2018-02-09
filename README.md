# cse-110-team-project-team-10

```
Song {
      // Use getters and setters for these
    private String title;
    private String artist;
    private String album;
    private int lastLocation;
    private long lastTime;
    private int preference;
    
    private Uri uri; // this will be the link to the song file. you can use this to load the song. Or if you want to do it in other ways 
                     // I can remove it.
                     
    public void rotatePreference(); // increment then % by 3
}
```

```
/* I know this is a weird name but you will interact with the database through this interface
 */
SongDao {

    public int queryLastLocation(String title, String artist, String album); //TODO check type
    
    public long queryLastTime(String title, String artist, String album);

    public int queryPreference(String title, String artist, String album);

    public Song isIntheDB(String title, String artist, String album);

    public void insertSong(Song song);

    public void updateSong(Song... song);

    public void insertAllSong(Song... songs);

    public void deleteSong(Song song);
}
```

```
How to use SongDao:
  Have the following as private fields in your class:
      private SongDao songDao;
      private SongDatabase Db;
      
  You have to execute these codes before you can use songDao: 
      Db = SongDatabase.getSongDatabase(getApplicationContext());
      songDao = Db.songDao();
      
      
  To query or update:
      long lasttime = songDao.query(whatever is in the header);
      songDao.update(song);
```

```
MainActivity {
    ArrayList<Song> songsList; // you need to somehow have a reference to this in the songService I think.
                               // Just assmue that this list contains all the songs
}
```
