// This is the main class

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.*;

import java.net.*;
import javax.swing.table.*;

import javax.imageio.ImageIO;
import java.time.Duration;

import java.nio.file.*;
import java.io.File;

import java.awt.*;
import java.util.*;

import java.util.concurrent.CancellationException;

import org.tpoly.msbte.*;
import java.awt.event.*;

public class ReportGenerator extends JFrame implements ActionListener
{

  CustomSlider cs = new CustomSlider();
  final KeyListener kL = new KeyAdapter(){

      int sB;

      public void keyPressed(KeyEvent e) {

       switch(e.getKeyCode())
       { 
         case KeyEvent.VK_UP:
         case KeyEvent.VK_LEFT:
	  sB = cs.getScreenNumber() - 1;
          if(sB>0) cs.animateToScreen(sB, 500);
	  break;

         case KeyEvent.VK_DOWN:
         case KeyEvent.VK_RIGHT:
	  sB = cs.getScreenNumber() + 1;
          if(sB<=3) cs.animateToScreen(sB, 500);
	  break;
       }
     } 
    };

  ReportGenerator()
  {
    if(Files.exists(Paths.get("./cache/secret"))) setContentPane(createPanel2());
    else
    {
      // (new File("./cache/secret")).mkdirs();
      final JPanel panel  = new JPanel();
      final JPanel panel1 = new JPanel();
      final JPanel panel2 = createPanel2();

    panel.setPreferredSize(new Dimension(768, 360));
    panel.setLayout(new GridLayout(1,2));

    panel.add(panel1);
    panel.add(panel2);

    panel1.setLayout(new BoxLayout(panel1, BoxLayout.Y_AXIS));
    panel1.addKeyListener(kL);
    panel1.setPreferredSize(new Dimension(384, 980));
    panel1.setSize(384, 402);

    panel1.add(cs);

    JLabel label = new JLabel("Generating reports for diploma students made easier");
    label.setAlignmentX(JLabel.CENTER_ALIGNMENT);
    panel1.add(label);

    panel1.add(Box.createVerticalStrut(10));

    final JScrollPane mSlider = new JScrollPane(panel);
    mSlider.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
    mSlider.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    mSlider.setBorder(BorderFactory.createEmptyBorder());

    getContentPane().add(mSlider);

    JButton button = new JButton("Continue");
    button.setAlignmentX(JLabel.CENTER_ALIGNMENT);
    button.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e)
      {
        // Animate to the other end
	Ticker t = new Ticker(){
	  int i = 0;
	  public void onTick(Ticker t, Duration duration)
	  {
	    if(++i==384) t.stop();
	    mSlider.getHorizontalScrollBar().setValue(i);
	    try{Thread.sleep(1);}catch(Exception e){}
	  }
	};
   	  t.start();
      }
     });
     button.addKeyListener(kL);
     panel1.add(button);

     panel1.add(Box.createVerticalStrut(12));
    }

    setSize(384, 402);
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setResizable(false);
    setTitle("MSBTE Report Generator");
    setVisible(true);
  }

  JLabel status = new JLabel("The above URL was auto-generated for Summer 2020.");
  JTextField sampleLinkField;
  JTextArea uidField = new JTextArea(10,10);
  JTextField oFNField = new JTextField(Paths.get(System.getProperty("user.dir"),"report.xlsx").toString());

  public JPanel createPanel2()
  {
    final JPanel panel = new JPanel();

    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setPreferredSize(new Dimension(384, 402));
    panel.setSize(384, 402);
    panel.add(Box.createVerticalStrut(8));

    // Sample Link
    JLabel label = new JLabel("Enter a sample / example link: ");
    label.setFont(new Font("Open Sans", Font.BOLD, 12));
    panel.add(box(label));

    sampleLinkField = new JTextField("https://msbte.org.in/CRSLDNOV2020DISRESLIVE/2FRSRESFLS20LIVE/EnrollmentNumber/17/1705220125Marksheet.html");
    sampleLinkField.addKeyListener(new KeyAdapter() {
    public void keyReleased(KeyEvent e) {
        try
        {
           final URL url = new URL(sampleLinkField.getText().trim());
           if(!url.getHost().equals("msbte.org.in")) status.setText("Please make sure the hostname is msbte.org.in");
           else if(!url.getProtocol().equals("https")) status.setText("Please explicitly specify the protocol as https.");
	   else if(!url.getFile().endsWith("Marksheet.html")) status.setText("Please check the link again (from start to end).");
           else status.setText("");
        }
	catch(Exception ex)
        {
	   status.setText("The format of the above URL doesn't seem valid. (https)");
	}
      }
    });
    panel.add(box(sampleLinkField));

    status.setFont(new Font("Open Sans", Font.PLAIN, 12));
    panel.add(box(status));

    panel.add(Box.createVerticalStrut(8));

    panel.add(box(new JLabel("Enter a list of UIDs (i.e. Enrollment/Seat Nos.):")));

    Box box = Box.createHorizontalBox();

   JScrollPane scroll = new JScrollPane(uidField, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

    scroll.setMaximumSize(new Dimension(342, uidField.getMaximumSize().height));
    uidField.setMaximumSize(new Dimension(342, uidField.getMaximumSize().height));

    box.add(scroll);
    uidField.setBorder(new LineBorder(Color.GRAY, 1));
 
    panel.add(box(scroll));

    panel.add(Box.createVerticalStrut(8));

    panel.add(box(new JLabel("Enter the name of the output file:")));

    Box outBox = Box.createHorizontalBox();
    oFNField.setMaximumSize(new Dimension(340, oFNField.getPreferredSize().height+2));
    outBox.add(oFNField);
    outBox.add(Box.createHorizontalStrut(4));
    JButton button = new JButton("Browse");
    button.setMaximumSize(new Dimension(button.getPreferredSize().width, button.getPreferredSize().height-3));
    button.addActionListener(new ActionListener(){
	public void actionPerformed(ActionEvent e)
	{
	  JFileChooser jFileChooser = new JFileChooser();
	  File cFile = new File(oFNField.getText());
	  if(cFile.getParent()!=null) cFile = new File(cFile.getParent());
	  jFileChooser.setCurrentDirectory(cFile);	  
	  jFileChooser.setDialogTitle("Choose the destination folder/file");
	  jFileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
	  jFileChooser.addChoosableFileFilter(new FileFilter(){
	    public String getDescription(){ return "Excel File or Directory";}
	    public boolean accept(File file)
	    {
              return file.isDirectory()||file.getName().endsWith(".xls")||file.getName().endsWith(".xlsx");
	    }
	  });
          jFileChooser.setAcceptAllFileFilterUsed(false);

	  if(jFileChooser.showOpenDialog(null)==JFileChooser.APPROVE_OPTION)
	  {
            String finalPath = jFileChooser.getSelectedFile().getAbsolutePath();
            if(jFileChooser.getSelectedFile().isDirectory()) finalPath += "\\report.xlsx";
            if(!(finalPath.endsWith(".xlsx")||finalPath.endsWith(".xls"))) finalPath += ".xlsx";
	    oFNField.setText(finalPath);
	  }
	}
    });
    outBox.add(button);
    panel.add(box(outBox));

    panel.add(Box.createVerticalStrut(8));

    panel.add(box(button = new JButton("Generate")));

    button.addActionListener(this);

    panel.add(Box.createVerticalStrut(8));

    return panel;
  }

  public JComponent box(JComponent component)
  {
    Box box = Box.createHorizontalBox();

    box.add(component);
    box.add(Box.createHorizontalStrut(12));

    component.setMaximumSize(new Dimension(340, component.getPreferredSize().height));
    box.setMaximumSize(new Dimension(360, component.getPreferredSize().height+4));
    component.setBorder(BorderFactory.createCompoundBorder(component.getBorder(), BorderFactory.createEmptyBorder(0, 4, 0, 4)));

    return box;
  }

  boolean isRunning = true;

  public void actionPerformed(ActionEvent e)
  {
    File file = new File(oFNField.getText());

    // JOptionPane.showMessageDialog(this, "Please enter a valid directory/file name", "Invalid Output Path", JOptionPane.WARNING_MESSAGE);

    // Check if the file exists
    if(file.exists())
    {
      if(file.isDirectory()) file = new File(Paths.get(oFNField.getText(),"report.xlsx").toString());
      else if(!file.canWrite())
      {
	JOptionPane.showMessageDialog(this, "Please close any application that is currently viewing the selected file", "The specified file is locked", JOptionPane.WARNING_MESSAGE);
	return;
      }

    }
    else
    {
      try
      {
	file.createNewFile();	
      }
      catch(Exception ee)
      {
	JOptionPane.showMessageDialog(this, "Please make sure that you enter a valid path", "Invalid Path", JOptionPane.WARNING_MESSAGE);
	return;
      }
    }

    String en = sampleLinkField.getText().trim();

    try
    {
	en = new Integer(Integer.parseInt(en.substring(en.length()-24,en.length()-14))).toString();
    }
    catch(Exception ee)
    {
	try
	{
	  en = new Integer(Integer.parseInt(en.substring(en.length()-20,en.length()-14))).toString();
	}
	catch(Exception ex)
	{
	  JOptionPane.showMessageDialog(this, "The sample link seems to be invalid. If the format of the link has changed (in the future), please contact the developer of this software.", "Invalid Sample Link", JOptionPane.WARNING_MESSAGE);
	  return;
	}
    }

    try
    {
      new Marksheet(Marksheet.getPrefixFrom(sampleLinkField.getText().trim()), en);
    }
    catch(Exception ee)
    {
      if(JOptionPane.showConfirmDialog(this, "Unable to generate marksheet from the sample link. Are you sure that you want to proceed?\nAll UIDs that fail with this pattern will be marked as invalid.", "Invalid Sample Link", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
      return;
    }

    final JDialog progressWindow = new JDialog(this, "", true);

    cs = new CustomSlider();
    progressWindow.add(cs);

    final ArrayList<String> uids = new ArrayList<String>();

    for(String uid : uidField.getText().split("\n"))
    {
      uid = uid.trim();
      if(uid.length()!=10||uid.length()!=6);
      try
      {
        Integer.parseInt(uid);
        uids.add(uid);
      } catch(Exception ex){}
    }

    if(uids.size()==0)
    {
      JOptionPane.showMessageDialog(this, "No valid UIDs (Enrollment No. or Seat No.) were detected.", "Could not generate report", JOptionPane.WARNING_MESSAGE);
      return;
    }

    JProgressBar jProgressBar = new JProgressBar(0, uids.size());
    jProgressBar.setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(16, 8, 0, 8), jProgressBar.getBorder()));
    jProgressBar.setMaximum(uids.size()+1);
    progressWindow.add(jProgressBar);

    JLabel label = new JLabel("Creating Excel Sheet...", JLabel.LEFT);
    JPanel box = new JPanel();
    box.setLayout(new FlowLayout(FlowLayout.LEFT));
    box.add(Box.createHorizontalStrut(4));
    box.add(label);
    box.setMaximumSize(new Dimension(box.getMaximumSize().width, label.getMinimumSize().height));
    progressWindow.add(box);

    DefaultTableModel invalidUIDModel = new DefaultTableModel(new String[][]{}, new String[]{"Invalid UIDs"}){ public boolean isCellEditable(int row, int column) {return false;} };

    JTable table = new JTable(invalidUIDModel);
    table.addKeyListener(new KeyAdapter(){

      int sB;

      public void keyPressed(KeyEvent e) {

       switch(e.getKeyCode())
       { 
         case KeyEvent.VK_LEFT:
	  sB = cs.getScreenNumber() - 1;
          if(sB>0) cs.animateToScreen(sB, 500);
	  break;

         case KeyEvent.VK_RIGHT:
	  sB = cs.getScreenNumber() + 1;
          if(sB<=3) cs.animateToScreen(sB, 500);
	  break;
       }
     } 
    });

    JScrollPane jScrollPane = new JScrollPane(table);
        
    table.setMaximumSize(new Dimension(jScrollPane.getMaximumSize().width, 105));
    jScrollPane.setMaximumSize(new Dimension(jScrollPane.getMaximumSize().width, 105));

    table.setPreferredSize(new Dimension(jScrollPane.getMaximumSize().width, 105));
    jScrollPane.setPreferredSize(new Dimension(jScrollPane.getMaximumSize().width, 105));

    jScrollPane.setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(8, 8, 8, 8), jScrollPane.getBorder()));
    progressWindow.add(jScrollPane);

    Box fB = Box.createHorizontalBox();

    fB.add(Box.createHorizontalGlue());
    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event)
      {
	isRunning = false;
	progressWindow.setVisible(false);
      }
    });
    fB.add(cancelButton);
    fB.add(Box.createHorizontalStrut(8));

    JButton doneButton = new JButton("Done");
    doneButton.setEnabled(false);
    fB.add(doneButton);
    doneButton.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event)
      {
	progressWindow.setVisible(false);
      }
    });    

    fB.add(Box.createHorizontalStrut(14));
    fB.setMaximumSize(new Dimension(fB.getMaximumSize().width, cancelButton.getMaximumSize().height));

    progressWindow.add(fB);

    progressWindow.add(Box.createVerticalStrut(8));

    progressWindow.addKeyListener(kL);

    progressWindow.setLayout(new BoxLayout(progressWindow.getContentPane(), BoxLayout.Y_AXIS));
    progressWindow.setBounds((int) getBounds().getX() + 250, (int) getBounds().getY()+200, 384, 500);
    progressWindow.setResizable(false);
    progressWindow.setTitle("Generating Report for " + uids.size() + " students...");

    final String fileName = file.getPath();

    progressWindow.addWindowListener(new WindowAdapter(){
      @Override
      public void windowOpened(WindowEvent e)
      {
        (new SwingWorker<Void,Void>(){

	  public Void doInBackground()
	  {
	    try
	    {
          Report.genAndObserve(sampleLinkField.getText(), uids.toArray(new String[uids.size()]), fileName, new ReportGeneratorObserver(){

	    int cUIDs = 0;

	    public void onInvalidUID(String invalidUID, Exception e)
	    {
	      if(!isRunning) throw new CancellationException();
	      jProgressBar.setValue(jProgressBar.getValue()+1);
	      invalidUIDModel.addRow(new String[]{invalidUID});
	    }

	    public void onSheetFound(String UID)
	    {
	      if(!isRunning) throw new CancellationException();
	      label.setText("Finding sheet for " + UID + "...");
	    }

	    public void onMarksheetGenStart(String UID)
	    {
	      if(!isRunning) throw new CancellationException();
              label.setText("Fetching entry for " + UID + "...");
	    }

	    public void onMarksheetGenSuccess(String UID)
	    {
	      if(!isRunning) throw new CancellationException();
	      label.setText("Retrieving entry for " + UID + "...");
	    }

	    public void onMarksheetEntrySuccess(String UID)
	    {
	      if(!isRunning) throw new CancellationException();
	      ++cUIDs;
	      jProgressBar.setValue(jProgressBar.getValue()+1);
	      label.setText("Inserted entry for " + UID + "...");
	    }

	    public void onMarksheetEntryFailed(String UID)
	    {
	      if(!isRunning) throw new CancellationException();
	      label.setText("Marksheet entry failed for " + UID + "...");
	    }

            public void onReportGenStart()
            {
	      if(!isRunning) throw new CancellationException();
              label.setText("Generating report for " + uids.size() + " students...");
	    }

            public void onReportGenDone(String finalPath)
            {
	      jProgressBar.setValue(jProgressBar.getValue()+1);
	      if(cUIDs==0)
	      {
		label.setText("No UID was valid. Could not generate a valid report.");
	        JOptionPane.showMessageDialog(progressWindow, "Please make sure that you only provide enrollment nos./seat nos. in a way that there is only one valid UID on each line.", "None of the passed UIDs were valid", JOptionPane.WARNING_MESSAGE);
	        progressWindow.setVisible(false);
		return;
	      }
              label.setText("Report Generated!");
              doneButton.setEnabled(true);
	      cancelButton.setVisible(false);

	      // Try opening the created spreadsheet with the default program of the OS
	      try{Desktop.getDesktop().open(new File(finalPath));}catch(Exception e){}
            }

	    boolean retried = false;

	    public String onOutputFileLocked(String outputFileName)
	    {
	      int opt;
	      if(retried) opt = JOptionPane.showOptionDialog(progressWindow, "How do you wish to proceed?", "Could not write to " + outputFileName, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, new Object[]{"Retry", "Cancel","Choose a New File"}, "Retry");
	      else
	      {
		opt = JOptionPane.showOptionDialog(progressWindow, "You probably have opened the specified file via some other program.\nPlease close that program and click on retry.", "Could not write to " + outputFileName, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, new Object[]{"Retry","Cancel"}, "Retry");
		retried = true;
	      }

              if(opt==0) return outputFileName;
	      else if(opt==1) return null;	      

	      JFileChooser jFileChooser = new JFileChooser();
	      File file = new File(outputFileName);
	      if(file.getParent()!=null) file = new File(file.getParent());
	      jFileChooser.setCurrentDirectory(file);	  
	      jFileChooser.setDialogTitle("Choose a new destination folder/file");
	      jFileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
	      jFileChooser.addChoosableFileFilter(new FileFilter(){
	        public String getDescription(){ return "Excel File or Directory";}
	        public boolean accept(File file)
	        {
                  return file.isDirectory()||file.getName().endsWith(".xls")||file.getName().endsWith(".xlsx");
	        }
	      });
              jFileChooser.setAcceptAllFileFilterUsed(false);

  	      if(jFileChooser.showOpenDialog(null)==JFileChooser.APPROVE_OPTION)
 	      {
                String finalPath = jFileChooser.getSelectedFile().getAbsolutePath();
                if(jFileChooser.getSelectedFile().isDirectory()) finalPath += "\\report.xlsx";
                if(!(finalPath.endsWith(".xlsx")||finalPath.endsWith(".xls"))) finalPath += ".xlsx";
	        return finalPath;
  	      }
              return null;
	    }

            public Boolean onConnectionIssue(String UID)
            {
	      if(!isRunning) throw new CancellationException();
              return JOptionPane.showConfirmDialog(progressWindow, "An issue occurred while fetching the marksheet of " + UID + ". Do you want to retry?", "Could not connect to www.msbte.org.in", JOptionPane.YES_NO_OPTION) == 0;
            }

            public Boolean onMarksheetNotFound(String UID)
            {
	      if(!isRunning) throw new CancellationException();
	      jProgressBar.setValue(jProgressBar.getValue()+1);
	      invalidUIDModel.addRow(new String[]{UID + " : Marksheet not found on server/cache"});
              return true;
            }
          }); 

	} catch(CancellationException ce) {
	  progressWindow.setVisible(false);
	  isRunning = true;
	}
	catch(Exception ex) {
	  System.out.println("Error thrown while report generation:");
	  ex.printStackTrace();
	}
            return null;
          }
	}).execute();
      }
    });

    progressWindow.setVisible(true);
  }

  public static void main(String args[]){new ReportGenerator();}
}

class CustomSlider extends JPanel
{
  private final Image introImage = Toolkit.getDefaultToolkit().getImage("res/4.png");
  private Ticker t;
  private int x = 0;
  private Thread checker = new Thread(){
    public void run()
    {
        while(true)
	{
           try{Thread.sleep(3000);}catch(Exception e){}
	   final int sn = getScreenNumber()+1;
           if(sn<4) animateToScreen(sn, 700);
           else animateToScreen(1, 700);
           try {Thread.sleep(4000);} catch(Exception e){}
        }
    }
  };

  private final MouseAdapter mA = new MouseAdapter(){

       int lastX = 0;
       Boolean dir = false;

       public void mouseReleased(MouseEvent e) {
	 autoAdjust();
         dir = null;
       }

       public void mouseDragged(MouseEvent e)
       {
	 Boolean newDir = lastX>e.getX();
	 if(dir!=newDir){
	  // if(dir!=null) System.out.println("Direction changed!");
	  lastX = e.getX();
 	 }
         dir = newDir;
         if(dir==null) return;
         int xx = x + (lastX-e.getX())/13;

         if(xx!=x && xx>=0 && xx<=774)
	 {
	   x=xx;
           repaint();
	 }
       }

       public void mousePressed(MouseEvent e) {
         lastX = e.getX();
       }

    };

  CustomSlider()
  {
    init();
    addMouseListener(mA);
    addMouseMotionListener(mA);
  }

  public int getScreenNumber()
  {
    if(x<194) return 1;
    if(x<580) return 2;
    return 3;
  }

  public void jumpTo(int offset)
  {
    if(t!=null&&!t.isStopped()) t.stop();
    x = offset;
    repaint();
  }

  public void autoAdjust()
  {
    final int sn = getScreenNumber();
    animateToScreen(sn, 700);
  }

  public void animateToScreen(int number, int animDuration)
  {
    final int offset = getOffsetForScreen(number);
    // System.out.println("Auto-adjusting to..."+offset);
    animateTo(offset, animDuration);    
  }

  public int getOffsetForScreen(int number)
  {
    switch(number)
    {
      case 1: return 0;
      case 2: return 387;
      case 3: return 774;
      default: return -1;
    }
  }

  public void animateTo(int offset, double animDuration)
  {
    if(offset!=x && offset<0 && offset>774) return;
    if(t!=null&&!t.isStopped()) t.stop();
    t = new Ticker(){

      int start = x;
      int diff  = offset-x;

      public void onTick(Ticker t, Duration duration)
      {
        double d = duration.toMillis()/animDuration;
	x = start + (int)(d*diff);
        if(d>=1)
        {
          t.stop();
          x = offset;
        }
	repaint();
      }
    };
    t.start();
  }

  private void init()
  {
    checker.start();
    Runtime.getRuntime().addShutdownHook(new Thread(){
      public void run()
      {
	checker.stop();
      }
    });
    setSize(introImage.getWidth(this), introImage.getHeight(this));
  }

  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    g.drawImage(introImage,-x, 0, introImage.getWidth(null), introImage.getHeight(null), this);
  }
}

class ImageSlider extends JPanel
{
  private final Image[] images;

  ImageSlider(final Image[] images)
  {
    this.images = images;
  }
}

abstract class Ticker
{
  private CustomWorker worker;

  abstract public void onTick(Ticker t, Duration d);

  public void start()
  {
    if(worker!=null) worker.cancel(false);
    worker = new CustomWorker(this){
      public Void doInBackground()
      {
        start = System.currentTimeMillis();
        while(!isCancelled()) 
          if(isPlaying) onTick(parent, Duration.ofMillis(System.currentTimeMillis()-start-pause));
        return null;
      }
    };
    worker.execute();
  }

  public void pause()
  {
    worker.pause();
  }

  public void play()
  {
    worker.play();
  }

  public void stop()
  {
    worker.cancel(false);
    worker = null;
  }

  public Boolean isStopped()
  {
    return worker==null;
  }

  public Boolean isPaused()
  {
    return worker==null?null:worker.isPaused();
  }

  public Boolean isPlaying()
  {
    return worker==null?null:worker.isPlaying();
  }
}

abstract class CustomWorker extends SwingWorker<Void, Void>
{
  public long start;
  public long pause = 0;
  public boolean isPlaying = true;
  public final Ticker parent;

  CustomWorker(final Ticker parent){this.parent=parent;}
  
  public void pause()
  {
    isPlaying = false;
    pause=System.currentTimeMillis()-pause;
  }

  public void play()
  {
    pause=System.currentTimeMillis()-pause;
    isPlaying=true;
  }

  public boolean isPaused(){return !isPlaying;}
  public boolean isPlaying(){return isPlaying;}
}