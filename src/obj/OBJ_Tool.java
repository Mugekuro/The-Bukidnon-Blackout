package obj;

import javax.imageio.ImageIO;
import java.util.Random;

public class OBJ_Tool extends SuperObject {

    public OBJ_Tool() {
        name = "Tool";

        // Default values
        toolPostGroup = 1;
        postNumber = 1;

        // Random tool type
        Random random = new Random();
        toolPostGroup = random.nextInt(4) + 1;

        try {
            String imageName = "objects/tool" + toolPostGroup + ".png";
            image = ImageIO.read(getClass().getClassLoader().getResourceAsStream(imageName));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}