package co.yoprice.nextgenchat;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import co.yoprice.nextgenchat.data.models.Message;
import co.yoprice.nextgenchat.utils.NGUClient;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void parse(){
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File("/home/cj/Documents/Android Projects/Contest/NextGenChat/app/src/main/res/raw/file2.txt"))));
            String s = null;
            StringBuilder sb = new StringBuilder();
            while ((s = br.readLine())!=null){
                sb.append(s);
            }
            ArrayList<Message> messages = NGUClient.Parse(sb.toString());

            for (Message m : messages) {
            //    System.out.printf("%s",m.getUser());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}