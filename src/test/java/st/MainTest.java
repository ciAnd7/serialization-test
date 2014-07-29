package st;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MainTest {

    static String json = "{\"type\":\"clist\",\"message\":\"msg\",\"date\":1395388393205,\"s_array\":[\"v1\",\"v2\",\"v3\"],\"i_array\":[1,2,3]}";

    static final int count = 1000000; // 1 millions
    
    @Test
    public void test_1() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        // ********* Do warming
        System.out.println("Warming...." + count);
        for (int i = 0; i < 1000; i++) {
            JsonNode tree = mapper.readTree(json);

            mapper.treeToValue(tree, MyBean.class);
            if (i % 10000 == 0) {
                //System.out.print(".");
            }
        }
        System.out.println("\nStarting test....");

        // ********* Do object mapping
        long start = System.currentTimeMillis();
        MyBean myBean = null;
        for (int i = 0; i < count; i++) {
            JsonNode tree = mapper.readTree(json);

            myBean = mapper.treeToValue(tree, MyBean.class);
            if (i % 10000 == 0) {
                //System.out.print(".");
            }
        }
        System.out.println("\nObjectMapper: " + (System.currentTimeMillis() - start));
        
        // Check
        check(myBean);
        

        // ******** Do manual mapping
        start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            JsonNode tree = mapper.readTree(json);

            myBean = new MyBean();
            
            myBean.setDate(new Date(tree.get("date").asLong()));
            myBean.setMessage(tree.get("message").asText());
            myBean.setType(tree.get("type").asText());
            
            JsonNode sArr = tree.get("s_array");
            HashSet<String> set = new HashSet<>();
            for (JsonNode n : sArr) {
                set.add(n.asText());
            }
            myBean.setS_array(set);
            
            JsonNode iArr = tree.get("i_array");
            ArrayList<Integer> list = new ArrayList<>();
            for (JsonNode n : iArr) {
                list.add(n.asInt());
            }
            myBean.setI_array(list);
            
            if (i % 10000 == 0) {
                //System.out.print(".");
            }
        }
        
        System.out.println("\nManualMapper: " + (System.currentTimeMillis() - start));
        
        check(myBean);
        
        // ********* Do Java Serialization
        
//        myBean.setI_array(null);
//        myBean.setS_array(null);
        
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bytes);
        out.writeObject(myBean); // Use existent bean instance
        out.close();
        byte[] byteObj = bytes.toByteArray();
        
        System.out.println("\nObject size: " + byteObj.length);
        
        start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            ObjectInputStream ins = new ObjectInputStream(new ByteArrayInputStream(byteObj));
            
            myBean = (MyBean) ins.readObject();
            
            if (i % 10000 == 0) {
//                System.out.print(".");
            }
            
            ins.close();
        }
        
        System.out.println("\nJavaSerialization: " + (System.currentTimeMillis() - start));

        check(myBean);
    }

    private static void check(MyBean myBean) {
        assertNotNull(myBean);
        assertNotNull(myBean.type);
        assertNotNull(myBean.date);
        assertNotNull(myBean.message);
        
        assertNotNull(myBean.i_array);
        assertTrue(myBean.i_array.size() == 3);
        assertTrue(myBean.i_array.get(0) == 1);
        assertNotNull(myBean.s_array);
        assertTrue(myBean.s_array.size() == 3);
        assertTrue(myBean.s_array.contains("v1"));
    }

    public static class MyBean implements Serializable {
        private static final long serialVersionUID = 1;
        
        String type;
        String message;
        Date date;
        Set<String> s_array;
        List<Integer> i_array;
        
        public MyBean() {
            
        }

        /**
         * @return the type
         */
        public String getType() {
            return type;
        }

        /**
         * @param type
         *            the type to set
         */
        public void setType(String type) {
            this.type = type;
        }

        /**
         * @return the message
         */
        public String getMessage() {
            return message;
        }

        /**
         * @param message
         *            the message to set
         */
        public void setMessage(String message) {
            this.message = message;
        }

        /**
         * @return the date
         */
        public Date getDate() {
            return date;
        }

        /**
         * @param date
         *            the date to set
         */
        public void setDate(Date date) {
            this.date = date;
        }

        /**
         * @return the s_array
         */
        public Set<String> getS_array() {
            return s_array;
        }

        /**
         * @param s_array
         *            the s_array to set
         */
        public void setS_array(Set<String> s_array) {
            this.s_array = s_array;
        }

        /**
         * @return the i_array
         */
        public List<Integer> getI_array() {
            return i_array;
        }

        /**
         * @param i_array
         *            the i_array to set
         */
        public void setI_array(List<Integer> i_array) {
            this.i_array = i_array;
        }

    }

}
