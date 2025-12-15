package obj;

import javax.imageio.ImageIO;

public class OBJ_Post extends SuperObject {
    public int postNumber = 1; // Default to post 1

    public OBJ_Post() {
        name = "Post";
        try {
            image = ImageIO.read(getClass().getClassLoader().getResourceAsStream("objects/post.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        collision = true; // Player can't walk through it
    }
}