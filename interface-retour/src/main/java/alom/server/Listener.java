package alom.server;
import jakarta.servlet.ServletContextListener;


public class Listener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("L'application démarre...");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("L'application s'arrête...");
        App.finish();
    }
    
}
