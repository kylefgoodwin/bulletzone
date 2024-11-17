package edu.unh.cs.cs619.bulletzone.web;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import edu.unh.cs.cs619.bulletzone.BulletZoneServer;
import edu.unh.cs.cs619.bulletzone.repository.InMemoryGameRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {BulletZoneServer.class})
public class GamesControllerTest {

    MockMvc mockMvc;
    @Mock
    private InMemoryGameRepository repo;
    @InjectMocks
    private GamesController gamesController;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(gamesController).build();
    }


    @Test
    public void testCreateGame() throws Exception {
        //mockMvc.perform(post("/games"))
        //        .andExpect(status().isCreated());
    }
}
