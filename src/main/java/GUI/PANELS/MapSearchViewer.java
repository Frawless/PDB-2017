/**
 * Projekt do předmětu PDB (2016/2017) - Prostorové, multimediální a temporální 
 * databáze: Metro Accident Databse
 * Autoři: Petr Staněk (xstane34), 
           František Matečný (xmatec00),
           Jakub Stejskal (xstejs24)
 * Datum:  12.12.2016
 * Verze:  1.0
 */
package GUI.PANELS;

import DB.Objects.DrawPoint;
import DB.Objects.DrawPolygon;
import DB.Objects.DrawRectangle;
import GUI.Main;
import LOGIC.Incident.TIncident;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import oracle.spatial.geometry.JGeometry;

public class MapSearchViewer extends javax.swing.JPanel {

    private int id_stanice;
    private TIncident incident;
    private List<DrawPoint> listPoints;
    List<Integer> listId_stanice;
    
    /**
     * Creates new form MapSearchViewer
     */
    public MapSearchViewer() {
        initComponents();
        listPoints = new ArrayList<>(); 
        listId_stanice = new ArrayList<>();

	// load the filtered objects from a db.
        try {
            loadFilteredObjectFromDb();
        } catch (Exception e) {
            e.printStackTrace();
        }    
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
   
    public void setFilter(int id)
    {
            this.id_stanice = id;
    }
   
    public void setIncident(TIncident ta)
    {
            this.incident = ta;
    }
    
    public void loadFilteredObjectFromDb() {
        listPoints.removeAll(listPoints);
        listId_stanice.removeAll(listId_stanice);
                
        String query = DB.Queries.Spatial.getIdStationFromIncidents(incident); 
        if(query != null){
            try (Statement stmt = Main.db.getConnection().createStatement())
            { 
                try (ResultSet rs = stmt.executeQuery(query))
                {
                    while (rs.next()) {
                        listId_stanice.add(rs.getInt("id_stanice"));
                    }
                }
            } catch (SQLException sqlEx) {
                    System.err.println("SQLException (Search-loadShapesFromDb()): " + sqlEx.getMessage());
            }

            for (Integer id_stanica : listId_stanice) {
                query = DB.Queries.Spatial.getGeometry(id_stanica, 4);
                try (Statement stmt = Main.db.getConnection().createStatement())
                    { 
                        try (ResultSet rs = stmt.executeQuery(query))
                        {
                            while (rs.next()) {
                                byte[] image = rs.getBytes("geometrie");
                                String color = rs.getString("barva");
                                String name = rs.getString("nazev");
                                int id = rs.getInt("id");
                                JGeometry jGeometry = JGeometry.load(image);
                                jGeometry2DrawPoint(jGeometry, null, id, name, color); 
                            }

                        }
                    } catch (SQLException sqlEx) {
                            System.err.println("SQLException (loadShapesFromDb()-stanica): " + sqlEx.getMessage());
                    }
                    catch (Exception sqlEx) {}
                }
        }
    }

    public void loadFilteredStationFromDb() {
        listPoints.removeAll(listPoints);
        listId_stanice.removeAll(listId_stanice);
        String query;
        
        listId_stanice.add(id_stanice);
        
        for (Integer id_stanica : listId_stanice) {
            query = DB.Queries.Spatial.getGeometry(id_stanica, 4);
            try (Statement stmt = Main.db.getConnection().createStatement())
                { 
                    try (ResultSet rs = stmt.executeQuery(query))
                    {
                        while (rs.next()) {
                            byte[] image = rs.getBytes("geometrie");
                            String color = rs.getString("barva");
                            String name = rs.getString("nazev");
                            int id = rs.getInt("id");
                            JGeometry jGeometry = JGeometry.load(image);
                            jGeometry2DrawPoint(jGeometry, null, id, name, color); 
                        }

                    }
                } catch (SQLException sqlEx) {
                        System.err.println("SQLException (loadShapesFromDb()-stanica): " + sqlEx.getMessage());
                }
                catch (Exception sqlEx) {}
            }
    }
    
    
    
    
    public void jGeometry2DrawPoint(JGeometry jGeometry,  String str, int id, String name, String color) {
        // check a type of JGeometry object
        switch (jGeometry.getType()) {
            // it is a point
            case JGeometry.GTYPE_POINT:
				listPoints.add(new DrawPoint());
				listPoints.get(listPoints.size()-1).p2d = jGeometry.getJavaPoint();
				listPoints.get(listPoints.size()-1).id = id;
				listPoints.get(listPoints.size()-1).name = name;
				listPoints.get(listPoints.size()-1).color = Color.decode(color);
				break;
            // it is something else (we do not know how to convert)
            default:
                System.err.println("Neznami typ geometrie: " + jGeometry.getType());  
        }
    }
    
    @Override
    public void paint(Graphics gg) {
        super.paint(gg);
        
        // a canvas of the Graphics context
        Graphics2D g = (Graphics2D) gg;
        g.scale(0.54, 0.54);
        
        // vykresli celu mapu
        g.setStroke(new BasicStroke(1));
        for (DrawPolygon p : Main.spacialObjects.listPolygons) {
            g.setPaint(Color.decode("#eee9e9"));
            g.fillPolygon(p.xpoints, p.ypoints, p.npoints);
            g.setPaint(Color.DARK_GRAY);
            g.drawPolygon(p.xpoints, p.ypoints, p.npoints); 
        }
        
        
        for (DrawRectangle p : Main.spacialObjects.listRectangles) {;
            g.setPaint(Color.decode("#eee9e9"));
            g.fillRect(p.x, p.y, p.width, p.height);
            g.setPaint(Color.DARK_GRAY);
            g.drawRect(p.x, p.y, p.width, p.height);
        }
        
        g.setStroke(new BasicStroke(3));
        g.setPaint(Color.GRAY);
        for (DrawPolygon p : Main.spacialObjects.listPolylines) {
            g.drawPolyline(p.xpoints, p.ypoints, p.npoints);
        }
        
        g.setStroke(new BasicStroke(1));
        for (DrawPoint p : Main.spacialObjects.listPoints) {
            g.setPaint(Color.GRAY);
            g.fillOval((int)p.p2d.getX()-4, (int) p.p2d.getY()-4, 8, 8);
            g.setPaint(Color.BLACK);
            g.drawOval((int)p.p2d.getX()-4, (int) p.p2d.getY()-4, 8, 8);   
        } 

        //zvyraznenie stanice
        for (DrawPoint p : listPoints) {
            g.setPaint(Color.RED);
            g.fillOval((int)p.p2d.getX()-6, (int) p.p2d.getY()-6, 12, 12);
        } 
    } 

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}