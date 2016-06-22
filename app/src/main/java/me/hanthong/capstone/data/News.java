package me.hanthong.capstone.data;

/**
 * Created by peet29 on 22/6/2559.
 */
public class News {
    public final String Title;
    public final String Link;
    public final String Date;
    public final String Description;
    public final String Content;
    public final String Photo;

    public News (String title,String link,String date,String description,String content,String photo)
    {
        this.Title = title;
        this.Link = link;
        this.Date = date;
        this.Description = description;
        this.Content = content;
        this.Photo = photo;
    }
}
