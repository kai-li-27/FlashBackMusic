# cse-110-team-project-team-10

```
Song {
      // Use getters and setters for these
    private String title;
    private String artist;
    private String album;
    private Location lastLocation;
    private Date lastTime;
    private int preference;    
    private Uri uri; // uri to the song file
    
    //DO NOT TOUCH THE FOLLOWING FIELDS!!!
    private double LastAltitude;
    private double LastLongitude;
    private long LastTimeLong;
    

                     
    public void rotatePreference(); // increment then % by 3
}
```


```
If you want to talk to database, you have to do through SongDao, the following is how:
  Have the following as private fields in your class:
      private SongDao songDao;
      
  You have to execute these codes before you can use songDao: 
      Db = SongDatabase.getSongDatabase(getApplicationContext());
      songDao = Db.songDao();
      
      
  To query or update:
      String title = songDao.query(whatever is in the header); // Check the SongDao class of what methods are available
      songDao.update(song); //Update is so easy, pass in the song object and it will figure out what to do
```
