package com.athanstyx.community;

import com.athanstyx.community.util.SensitiveFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class testSensitiveFilter {

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Test
    public void testSensitiveFilter() {
        String test = "这里可以吸毒,可以嫖娼, 可以开票, 可以赌博,,,,阿斯达";
        String filter = sensitiveFilter.filter(test);
        System.out.println(filter);

        test = "这里可以↓吸↓毒↓,可以嫖↓娼, 可以↓开↓票↓, 可以赌↓博,,,,阿斯达";
        filter = sensitiveFilter.filter(test);
        System.out.println(filter);
    }

}
