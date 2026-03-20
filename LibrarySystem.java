import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class LibrarySystem {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AppWindow().setVisible(true));
    }

    // ─────────────────────────────────────────────────────────────
    //  THEME
    // ─────────────────────────────────────────────────────────────
    static final Color BG_DARK      = new Color(10, 12, 20);
    static final Color BG_PANEL     = new Color(16, 20, 34);
    static final Color BG_CARD      = new Color(22, 28, 48);
    static final Color BG_INPUT     = new Color(28, 35, 58);
    static final Color BG_ROW_ALT   = new Color(19, 24, 42);
    static final Color ACCENT       = new Color(255, 185, 50);
    static final Color ACCENT_DIM   = new Color(180, 125, 20);
    static final Color TEXT_PRIMARY  = new Color(230, 235, 255);
    static final Color TEXT_SECONDARY= new Color(130, 145, 185);
    static final Color TEXT_MUTED    = new Color(70, 85, 125);
    static final Color SUCCESS      = new Color(60, 210, 140);
    static final Color DANGER       = new Color(255, 90, 90);
    static final Color INFO         = new Color(80, 160, 255);
    static final Color BORDER       = new Color(35, 45, 75);
    static final Font  F_TITLE      = new Font("Segoe UI", Font.BOLD, 26);
    static final Font  F_HEAD       = new Font("Segoe UI", Font.BOLD, 15);
    static final Font  F_BODY       = new Font("Segoe UI", Font.PLAIN, 13);
    static final Font  F_SMALL      = new Font("Segoe UI", Font.PLAIN, 11);
    static final Font  F_LABEL      = new Font("Segoe UI", Font.BOLD, 11);

    // ─────────────────────────────────────────────────────────────
    //  MODELS
    // ─────────────────────────────────────────────────────────────
    static class Book {
        private static int counter = 1;
        int id; String title, author, genre; int year; boolean available = true;
        Book(String t, String a, String g, int y) { id=counter++; title=t; author=a; genre=g; year=y; }
        public String toString() { return title + " — " + author; }
    }

    static class Member {
        private static int counter = 1;
        int id; String name, email; List<Book> borrowed = new ArrayList<>();
        Member(String n, String e) { id=counter++; name=n; email=e; }
        void borrow(Book b)  { borrowed.add(b);    b.available=false; }
        void returnBook(Book b){ borrowed.remove(b); b.available=true;  }
        public String toString() { return name + " (" + email + ")"; }
    }

    static class Library {
        List<Book>   books   = new ArrayList<>();
        List<Member> members = new ArrayList<>();
        Library() { seed(); }
        void seed() {
            books.add(new Book("The Pragmatic Programmer","Andrew Hunt","Technology",1999));
            books.add(new Book("Clean Code","Robert C. Martin","Technology",2008));
            books.add(new Book("1984","George Orwell","Dystopian",1949));
            books.add(new Book("Dune","Frank Herbert","Science Fiction",1965));
            books.add(new Book("The Great Gatsby","F. Scott Fitzgerald","Classic",1925));
            books.add(new Book("To Kill a Mockingbird","Harper Lee","Classic",1960));
            books.add(new Book("Sapiens","Yuval Noah Harari","History",2011));
            books.add(new Book("Atomic Habits","James Clear","Self-Help",2018));
            books.add(new Book("The Hobbit","J.R.R. Tolkien","Fantasy",1937));
            books.add(new Book("Brave New World","Aldous Huxley","Dystopian",1932));
            members.add(new Member("Alice Sharma","alice@mail.com"));
            members.add(new Member("Rohan Mehra","rohan@mail.com"));
            members.add(new Member("Priya Kapoor","priya@mail.com"));
        }
        List<Book>   searchBooks(String q)   { String l=q.toLowerCase(); return books.stream().filter(b->b.title.toLowerCase().contains(l)||b.author.toLowerCase().contains(l)||b.genre.toLowerCase().contains(l)).collect(Collectors.toList()); }
        List<Member> searchMembers(String q) { String l=q.toLowerCase(); return members.stream().filter(m->m.name.toLowerCase().contains(l)||m.email.toLowerCase().contains(l)).collect(Collectors.toList()); }
        int totalBooks()    { return books.size(); }
        int available()     { return (int)books.stream().filter(b->b.available).count(); }
        int totalMembers()  { return members.size(); }
        int activeBorrows() { return (int)books.stream().filter(b->!b.available).count(); }
    }

    // ─────────────────────────────────────────────────────────────
    //  SHARED UI HELPERS
    // ─────────────────────────────────────────────────────────────
    static JTextField styledField(String ph) {
        JTextField f = new JTextField() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_INPUT); g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                if(getText().isEmpty()&&!isFocusOwner()){g2.setColor(TEXT_MUTED);g2.setFont(F_BODY);FontMetrics fm=g2.getFontMetrics();g2.drawString(ph,12,(getHeight()+fm.getAscent()-fm.getDescent())/2);}
                g2.dispose(); super.paintComponent(g);
            }
            protected void paintBorder(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isFocusOwner()?ACCENT:BORDER);
                g2.setStroke(new BasicStroke(isFocusOwner()?1.5f:1f));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,10,10); g2.dispose();
            }
        };
        f.setOpaque(false); f.setForeground(TEXT_PRIMARY); f.setCaretColor(ACCENT);
        f.setFont(F_BODY); f.setBorder(BorderFactory.createEmptyBorder(8,12,8,12));
        f.setPreferredSize(new Dimension(0,38));
        f.addFocusListener(new FocusAdapter(){ public void focusGained(FocusEvent e){f.repaint();} public void focusLost(FocusEvent e){f.repaint();}});
        return f;
    }

    static JButton primaryBtn(String txt) {
        JButton b = new JButton(txt) {
            float hov=0;
            javax.swing.Timer t;
            {
                setOpaque(false);setContentAreaFilled(false);setFocusPainted(false);
                setBorderPainted(false);setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setFont(F_HEAD);setForeground(BG_DARK);
                addMouseListener(new MouseAdapter(){
                    public void mouseEntered(MouseEvent e){anim(true);}
                    public void mouseExited(MouseEvent e){anim(false);}
                });
            }
            void anim(boolean in){
                if(t!=null)t.stop();
                t=new javax.swing.Timer(16,null);
                t.addActionListener(ev->{hov=in?Math.min(1f,hov+0.1f):Math.max(0f,hov-0.1f);repaint();if((in&&hov>=1f)||(!in&&hov<=0f))t.stop();});
                t.start();
            }
            protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                int r1=ACCENT.getRed(),g1=ACCENT.getGreen(),b1=ACCENT.getBlue();
                int r2=ACCENT_DIM.getRed(),g2c=ACCENT_DIM.getGreen(),b2=ACCENT_DIM.getBlue();
                g2.setColor(new Color((int)(r1+(r2-r1)*hov*0.3),(int)(g1+(g2c-g1)*hov*0.3),(int)(b1+(b2-b1)*hov*0.3)));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                if(hov>0){g2.setColor(new Color(255,255,255,(int)(30*hov)));g2.fillRoundRect(0,0,getWidth(),getHeight()/2,10,10);}
                g2.dispose(); super.paintComponent(g);
            }
        };
        b.setPreferredSize(new Dimension(130,38)); return b;
    }

    static JButton ghostBtn(String txt) {
        JButton b = new JButton(txt) {
            {setOpaque(false);setContentAreaFilled(false);setFocusPainted(false);setBorderPainted(false);setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));setFont(F_BODY);setForeground(TEXT_SECONDARY);}
            protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BORDER);g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,10,10); g2.dispose(); super.paintComponent(g);
            }
        };
        b.setPreferredSize(new Dimension(100,36)); return b;
    }

    static JPanel card() {
        return new JPanel() {
            {setOpaque(false);}
            protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);g2.fillRoundRect(0,0,getWidth(),getHeight(),14,14);
                g2.setColor(BORDER);g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,14,14); g2.dispose();
            }
        };
    }

    static JLabel sectionTitle(String t) { JLabel l=new JLabel(t);l.setFont(F_HEAD);l.setForeground(ACCENT);return l; }
    static JLabel bodyLabel(String t)    { JLabel l=new JLabel(t);l.setFont(F_BODY);l.setForeground(TEXT_SECONDARY);return l; }
    static JLabel fieldLabel(String t)   { JLabel l=new JLabel(t);l.setFont(F_LABEL);l.setForeground(TEXT_SECONDARY);l.setBorder(BorderFactory.createEmptyBorder(6,2,2,0));return l; }

    static JScrollPane styledScroll(Component v) {
        JScrollPane sp=new JScrollPane(v);
        sp.setOpaque(false);sp.getViewport().setOpaque(false);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getVerticalScrollBar().setUI(new BasicScrollBarUI(){
            protected void configureScrollBarColors(){thumbColor=BORDER;trackColor=BG_DARK;}
            protected JButton createDecreaseButton(int o){JButton b=new JButton();b.setPreferredSize(new Dimension(0,0));return b;}
            protected JButton createIncreaseButton(int o){JButton b=new JButton();b.setPreferredSize(new Dimension(0,0));return b;}
            protected void paintThumb(Graphics g,JComponent c,Rectangle r){Graphics2D g2=(Graphics2D)g.create();g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);g2.setColor(isDragging?ACCENT_DIM:TEXT_MUTED);g2.fillRoundRect(r.x+2,r.y+2,r.width-4,r.height-4,6,6);g2.dispose();}
            protected void paintTrack(Graphics g,JComponent c,Rectangle r){g.setColor(BG_DARK);g.fillRect(r.x,r.y,r.width,r.height);}
        });
        sp.setBackground(BG_PANEL); return sp;
    }

    static JComboBox<String> styledCombo(String... items) {
        JComboBox<String> c=new JComboBox<>(items);
        c.setBackground(BG_INPUT);c.setForeground(TEXT_PRIMARY);c.setFont(F_BODY);
        c.setPreferredSize(new Dimension(0,38));
        c.setRenderer(new DefaultListCellRenderer(){
            public Component getListCellRendererComponent(JList<?> l,Object v,int i,boolean sel,boolean foc){
                super.getListCellRendererComponent(l,v,i,sel,foc);
                setBackground(sel?BG_INPUT:BG_CARD);setForeground(sel?ACCENT:TEXT_PRIMARY);
                setFont(F_BODY);setBorder(BorderFactory.createEmptyBorder(6,12,6,12));return this;
            }
        });
        return c;
    }

    static void styleTable(JTable t) {
        t.setBackground(BG_CARD);t.setForeground(TEXT_PRIMARY);t.setFont(F_BODY);
        t.setRowHeight(42);t.setShowGrid(false);t.setIntercellSpacing(new Dimension(0,1));
        t.setSelectionBackground(new Color(255,185,50,50));t.setSelectionForeground(TEXT_PRIMARY);
        t.setFillsViewportHeight(true);
        JTableHeader h=t.getTableHeader();
        h.setBackground(BG_DARK);h.setForeground(ACCENT);
        h.setFont(new Font("Segoe UI",Font.BOLD,11));
        h.setBorder(BorderFactory.createMatteBorder(0,0,1,0,BORDER));
        h.setReorderingAllowed(false);
        ((DefaultTableCellRenderer)h.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);
        DefaultTableCellRenderer lr=new DefaultTableCellRenderer();
        lr.setBorder(BorderFactory.createEmptyBorder(0,12,0,0));
        for(int i=0;i<t.getColumnCount();i++) t.getColumnModel().getColumn(i).setCellRenderer(lr);
    }

    static void shake(JDialog d) {
        int x=d.getLocation().x,y=d.getLocation().y;
        int[] offs={-8,8,-6,6,-4,4,-2,2,0};
        int[] i={0};
        javax.swing.Timer[] t={null};
        t[0]=new javax.swing.Timer(30,e->{ if(i[0]<offs.length) d.setLocation(x+offs[i[0]++],y); else{d.setLocation(x,y);t[0].stop();}});
        t[0].start();
    }

    // ─────────────────────────────────────────────────────────────
    //  MAIN WINDOW
    // ─────────────────────────────────────────────────────────────
    static class AppWindow extends JFrame {
        Library lib = new Library();
        JPanel content;
        DashPanel dash; BooksPanel books; MembersPanel members; BorrowPanel borrow;
        NavBtn[] navBtns; int activeNav = 0;

        AppWindow() {
            setTitle("Bibliotheca"); setDefaultCloseOperation(EXIT_ON_CLOSE);
            setSize(1200,760); setMinimumSize(new Dimension(900,600));
            setLocationRelativeTo(null); setUndecorated(true);
            setBackground(new Color(0,0,0,0));

            JPanel root = new JPanel(new BorderLayout()) {
                protected void paintComponent(Graphics g){
                    Graphics2D g2=(Graphics2D)g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(BG_DARK); g2.fillRoundRect(0,0,getWidth(),getHeight(),16,16); g2.dispose();
                }
                {setOpaque(false);}
            };

            root.add(buildTitleBar(), BorderLayout.NORTH);
            root.add(buildSidebar(),  BorderLayout.WEST);
            root.add(buildContent(),  BorderLayout.CENTER);

            setContentPane(root); getRootPane().setOpaque(false);
            drag(root); showPanel(0);
        }

        JPanel buildTitleBar() {
            JPanel bar=new JPanel(new BorderLayout());
            bar.setOpaque(false); bar.setPreferredSize(new Dimension(0,36));
            bar.setBorder(BorderFactory.createEmptyBorder(8,16,0,12));

            JLabel logo = new JLabel() {
                protected void paintComponent(Graphics g){
                    Graphics2D g2=(Graphics2D)g.create();
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2.setFont(new Font("Segoe UI",Font.BOLD,13)); g2.setColor(ACCENT); g2.drawString("BIBLIOTHECA",0,13);
                    g2.setFont(new Font("Segoe UI",Font.PLAIN,9)); g2.setColor(TEXT_MUTED); g2.drawString("LIBRARY MANAGEMENT SYSTEM",1,24);
                    g2.dispose();
                }
                {setPreferredSize(new Dimension(220,28));}
            };

            JPanel ctrl=new JPanel(new FlowLayout(FlowLayout.RIGHT,6,0)); ctrl.setOpaque(false);
            ctrl.add(dot(new Color(255,95,86),  e->System.exit(0)));
            ctrl.add(dot(new Color(255,189,46), e->setState(ICONIFIED)));
            ctrl.add(dot(new Color(39,201,63),  e->{ if(getExtendedState()==MAXIMIZED_BOTH) setExtendedState(NORMAL); else setExtendedState(MAXIMIZED_BOTH);}));
            bar.add(logo,BorderLayout.WEST); bar.add(ctrl,BorderLayout.EAST);
            return bar;
        }

        JButton dot(Color c, ActionListener al) {
            JButton b=new JButton(){
                {setOpaque(false);setContentAreaFilled(false);setBorderPainted(false);setFocusPainted(false);setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));setPreferredSize(new Dimension(16,16));}
                protected void paintComponent(Graphics g){Graphics2D g2=(Graphics2D)g.create();g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);g2.setColor(getModel().isRollover()?c.brighter():c);g2.fillOval(0,0,15,15);g2.dispose();}
            };
            b.addActionListener(al); return b;
        }

        JPanel buildSidebar() {
            JPanel side=new JPanel(){
                protected void paintComponent(Graphics g){
                    Graphics2D g2=(Graphics2D)g.create();
                    g2.setColor(BG_PANEL);g2.fillRect(0,0,getWidth(),getHeight());
                    g2.setColor(BORDER);g2.setStroke(new BasicStroke(0.5f));g2.drawLine(getWidth()-1,0,getWidth()-1,getHeight());g2.dispose();
                }
            };
            side.setOpaque(false); side.setPreferredSize(new Dimension(220,0));
            side.setLayout(new BoxLayout(side,BoxLayout.Y_AXIS));
            side.setBorder(BorderFactory.createEmptyBorder(52,0,20,0));

            String[][] nav={{"◈","Dashboard"},{"▣","Books"},{"◉","Members"},{"⇄","Borrow"}};
            navBtns=new NavBtn[nav.length];
            for(int i=0;i<nav.length;i++){navBtns[i]=new NavBtn(nav[i][0],nav[i][1],i);side.add(navBtns[i]);}
            side.add(Box.createVerticalGlue());
            JLabel ver=new JLabel("v1.0.0");ver.setFont(F_SMALL);ver.setForeground(TEXT_MUTED);
            ver.setAlignmentX(CENTER_ALIGNMENT);ver.setBorder(BorderFactory.createEmptyBorder(0,0,8,0));
            side.add(ver); return side;
        }

        JPanel buildContent() {
            content=new JPanel(new CardLayout()); content.setOpaque(false);
            Runnable r=this::refreshAll;
            dash=new DashPanel(lib); books=new BooksPanel(lib,r);
            members=new MembersPanel(lib,r); borrow=new BorrowPanel(lib,r);
            content.add(dash,"0"); content.add(books,"1"); content.add(members,"2"); content.add(borrow,"3");
            return content;
        }

        void showPanel(int idx) {
            activeNav=idx;
            ((CardLayout)content.getLayout()).show(content,String.valueOf(idx));
            for(NavBtn b:navBtns) b.repaint();
            if(idx==0) dash.refresh(); if(idx==3) borrow.refresh();
        }

        void refreshAll() { dash.refresh();books.refresh();members.refresh();borrow.refresh(); }

        void drag(JComponent c) {
            int[] d={0,0};
            c.addMouseListener(new MouseAdapter(){ public void mousePressed(MouseEvent e){d[0]=e.getX();d[1]=e.getY();}});
            c.addMouseMotionListener(new MouseMotionAdapter(){ public void mouseDragged(MouseEvent e){setLocation(getX()+e.getX()-d[0],getY()+e.getY()-d[1]);}});
        }

        class NavBtn extends JComponent {
            String icon,label; int idx; float hov=0;
            javax.swing.Timer ht;
            NavBtn(String icon,String label,int idx){
                this.icon=icon;this.label=label;this.idx=idx;
                setMaximumSize(new Dimension(220,52));setPreferredSize(new Dimension(220,52));
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                addMouseListener(new MouseAdapter(){
                    public void mouseClicked(MouseEvent e){showPanel(idx);}
                    public void mouseEntered(MouseEvent e){anim(true);}
                    public void mouseExited(MouseEvent e){anim(false);}
                });
            }
            void anim(boolean in){
                if(ht!=null)ht.stop();
                ht=new javax.swing.Timer(16,null);
                ht.addActionListener(e->{hov=in?Math.min(1f,hov+0.12f):Math.max(0f,hov-0.12f);repaint();if((in&&hov>=1f)||(!in&&hov<=0f))ht.stop();});
                ht.start();
            }
            protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                boolean act=activeNav==idx;
                if(act){g2.setColor(new Color(255,185,50,18));g2.fillRect(0,0,getWidth(),getHeight());g2.setColor(ACCENT);g2.setStroke(new BasicStroke(2.5f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));g2.drawLine(0,6,0,getHeight()-6);}
                else if(hov>0){g2.setColor(new Color(255,255,255,(int)(10*hov)));g2.fillRect(0,0,getWidth(),getHeight());}
                Color ic=act?ACCENT:blend(TEXT_MUTED,TEXT_SECONDARY,hov);
                g2.setFont(new Font("Segoe UI Symbol",Font.PLAIN,16));g2.setColor(ic);
                FontMetrics fm=g2.getFontMetrics();g2.drawString(icon,24,getHeight()/2+fm.getAscent()/2-2);
                g2.setFont(act?new Font("Segoe UI",Font.BOLD,13):new Font("Segoe UI",Font.PLAIN,13));
                g2.setColor(act?TEXT_PRIMARY:blend(TEXT_MUTED,TEXT_SECONDARY,hov));
                fm=g2.getFontMetrics();g2.drawString(label,58,getHeight()/2+fm.getAscent()/2-2); g2.dispose();
            }
            Color blend(Color a,Color b,float t){ return new Color((int)(a.getRed()+(b.getRed()-a.getRed())*t),(int)(a.getGreen()+(b.getGreen()-a.getGreen())*t),(int)(a.getBlue()+(b.getBlue()-a.getBlue())*t)); }
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  DASHBOARD
    // ─────────────────────────────────────────────────────────────
    static class DashPanel extends JPanel {
        Library lib;
        DashPanel(Library l){lib=l;setOpaque(false);setLayout(new BorderLayout(0,24));setBorder(BorderFactory.createEmptyBorder(30,30,30,30));build();}
        void build(){
            JLabel title=new JLabel("Dashboard");title.setFont(F_TITLE);title.setForeground(TEXT_PRIMARY);
            JLabel sub=new JLabel("Welcome back — here's your library at a glance");sub.setFont(F_BODY);sub.setForeground(TEXT_SECONDARY);
            JPanel hdr=new JPanel(new GridLayout(2,1,0,4));hdr.setOpaque(false);
            hdr.add(title);hdr.add(sub);hdr.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));
            add(hdr,BorderLayout.NORTH); add(buildGrid(),BorderLayout.CENTER);
        }
        JPanel buildGrid(){
            JPanel p=new JPanel(new GridBagLayout());p.setOpaque(false);
            GridBagConstraints g=new GridBagConstraints();g.fill=GridBagConstraints.BOTH;g.insets=new Insets(8,8,8,8);
            g.gridx=0;g.gridy=0;g.weightx=1;g.weighty=0.3; p.add(statCard("Total Books",lib.totalBooks(),ACCENT,"▣"),g);
            g.gridx=1; p.add(statCard("Available",lib.available(),SUCCESS,"✓"),g);
            g.gridx=2; p.add(statCard("Members",lib.totalMembers(),INFO,"◉"),g);
            g.gridx=3; p.add(statCard("Borrowed",lib.activeBorrows(),DANGER,"↗"),g);
            g.gridx=0;g.gridy=1;g.gridwidth=2;g.weighty=0.7; p.add(buildRecentBooks(),g);
            g.gridx=2;g.gridwidth=2; p.add(buildGenreChart(),g);
            return p;
        }
        JPanel statCard(String label,int val,Color accent,String icon){
            JPanel c=new JPanel(){
                protected void paintComponent(Graphics g){
                    Graphics2D g2=(Graphics2D)g.create();g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(BG_CARD);g2.fillRoundRect(0,0,getWidth(),getHeight(),14,14);
                    g2.setColor(new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),25));g2.fillRoundRect(0,0,getWidth(),getHeight(),14,14);
                    g2.setColor(new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),60));g2.setStroke(new BasicStroke(1f));g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,14,14);
                    g2.setColor(accent);g2.fillRoundRect(0,0,getWidth(),3,3,3); g2.dispose(); super.paintComponent(g);
                }
                {setOpaque(false);}
            };
            c.setLayout(new BorderLayout());c.setBorder(BorderFactory.createEmptyBorder(18,20,18,20));
            JLabel ico=new JLabel(icon);ico.setFont(new Font("Segoe UI Emoji",Font.PLAIN,22));ico.setForeground(accent);
            JLabel num=new JLabel(String.valueOf(val));num.setFont(new Font("Segoe UI",Font.BOLD,34));num.setForeground(TEXT_PRIMARY);
            JLabel lbl=new JLabel(label.toUpperCase());lbl.setFont(new Font("Segoe UI",Font.BOLD,10));lbl.setForeground(TEXT_SECONDARY);
            JPanel info=new JPanel(new GridLayout(3,1,0,2));info.setOpaque(false);info.add(ico);info.add(num);info.add(lbl);
            c.add(info,BorderLayout.CENTER); return c;
        }
        JPanel buildRecentBooks(){
            JPanel p=card();p.setLayout(new BorderLayout(0,12));p.setBorder(BorderFactory.createEmptyBorder(18,20,18,20));
            p.add(sectionTitle("Recent Books"),BorderLayout.NORTH);
            JPanel list=new JPanel();list.setOpaque(false);list.setLayout(new BoxLayout(list,BoxLayout.Y_AXIS));
            int show=Math.min(5,lib.books.size());
            for(int i=lib.books.size()-1;i>=lib.books.size()-show;i--){list.add(bookRow(lib.books.get(i)));list.add(Box.createVerticalStrut(5));}
            p.add(styledScroll(list),BorderLayout.CENTER); return p;
        }
        JPanel bookRow(Book b){
            JPanel row=new JPanel(new BorderLayout(10,0)){
                protected void paintComponent(Graphics g){Graphics2D g2=(Graphics2D)g.create();g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);g2.setColor(BG_ROW_ALT);g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);g2.dispose();}
                {setOpaque(false);setBorder(BorderFactory.createEmptyBorder(8,12,8,12));}
            };
            JLabel name=new JLabel(b.title);name.setFont(F_BODY);name.setForeground(TEXT_PRIMARY);
            JLabel auth=new JLabel(b.author);auth.setFont(F_SMALL);auth.setForeground(TEXT_SECONDARY);
            JPanel left=new JPanel(new GridLayout(2,1));left.setOpaque(false);left.add(name);left.add(auth);
            JLabel status=new JLabel(b.available?"Available":"Borrowed");status.setFont(F_SMALL);status.setForeground(b.available?SUCCESS:DANGER);
            row.add(left,BorderLayout.CENTER);row.add(status,BorderLayout.EAST); return row;
        }
        JPanel buildGenreChart(){
            JPanel p=card();p.setLayout(new BorderLayout(0,12));p.setBorder(BorderFactory.createEmptyBorder(18,20,18,20));
            p.add(sectionTitle("Books by Genre"),BorderLayout.NORTH);
            Map<String,Integer> gc=new LinkedHashMap<>();
            lib.books.forEach(b->gc.merge(b.genre,1,Integer::sum));
            Color[] pal={ACCENT,INFO,SUCCESS,DANGER,new Color(180,100,255),new Color(255,150,80)};
            JPanel chart=new JPanel(){
                protected void paintComponent(Graphics g){
                    super.paintComponent(g);
                    Graphics2D g2=(Graphics2D)g.create();g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                    int tot=gc.values().stream().mapToInt(Integer::intValue).sum();
                    int cx=getWidth()/2-30,cy=getHeight()/2,r=Math.min(cx,cy)-20;
                    double ang=-Math.PI/2; int i=0;
                    for(Map.Entry<String,Integer> e:gc.entrySet()){
                        double sw=2*Math.PI*e.getValue()/tot;
                        g2.setColor(pal[i%pal.length]);g2.fill(new Arc2D.Double(cx-r,cy-r,2*r,2*r,Math.toDegrees(ang),Math.toDegrees(sw),Arc2D.PIE));
                        g2.setColor(BG_CARD);g2.setStroke(new BasicStroke(2f));g2.draw(new Arc2D.Double(cx-r,cy-r,2*r,2*r,Math.toDegrees(ang),Math.toDegrees(sw),Arc2D.PIE));
                        ang+=sw; i++;
                    }
                    int hole=r/2;g2.setColor(BG_CARD);g2.fillOval(cx-hole,cy-hole,hole*2,hole*2);
                    int lx=cx+r+16,ly=cy-gc.size()*11; i=0;
                    for(Map.Entry<String,Integer> e:gc.entrySet()){
                        g2.setColor(pal[i%pal.length]);g2.fillRoundRect(lx,ly+i*22,10,10,3,3);
                        g2.setColor(TEXT_SECONDARY);g2.setFont(F_SMALL);g2.drawString(e.getKey()+" ("+e.getValue()+")",lx+16,ly+i*22+10); i++;
                    }
                    g2.dispose();
                }
            };
            chart.setOpaque(false);p.add(chart,BorderLayout.CENTER); return p;
        }
        void refresh(){removeAll();build();revalidate();repaint();}
    }

    // ─────────────────────────────────────────────────────────────
    //  BOOKS PANEL
    // ─────────────────────────────────────────────────────────────
    static class BooksPanel extends JPanel {
        Library lib; Runnable onRefresh; DefaultTableModel model; JTable table; JTextField search;
        BooksPanel(Library l,Runnable r){lib=l;onRefresh=r;setOpaque(false);setLayout(new BorderLayout());setBorder(BorderFactory.createEmptyBorder(30,30,30,30));build();}
        void build(){ add(topBar(),BorderLayout.NORTH); add(buildTable(),BorderLayout.CENTER); }
        JPanel topBar(){
            JPanel bar=new JPanel(new BorderLayout(12,0));bar.setOpaque(false);bar.setBorder(BorderFactory.createEmptyBorder(0,0,18,0));
            JLabel t=new JLabel("Books");t.setFont(F_TITLE);t.setForeground(TEXT_PRIMARY);
            search=styledField("Search books…");search.setPreferredSize(new Dimension(240,38));
            search.addKeyListener(new KeyAdapter(){ public void keyReleased(KeyEvent e){filterTable(search.getText());}});
            JButton add=primaryBtn("+ Add Book");add.setPreferredSize(new Dimension(130,38));add.addActionListener(e->showAdd());
            JPanel right=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));right.setOpaque(false);right.add(search);right.add(add);
            bar.add(t,BorderLayout.WEST);bar.add(right,BorderLayout.EAST); return bar;
        }
        JScrollPane buildTable(){
            model=new DefaultTableModel(new String[]{"ID","Title","Author","Genre","Year","Status"},0){public boolean isCellEditable(int r,int c){return false;}};
            table=new JTable(model){
                public Component prepareRenderer(TableCellRenderer r,int row,int col){
                    Component c=super.prepareRenderer(r,row,col);
                    c.setBackground(row%2==0?BG_CARD:BG_ROW_ALT);c.setForeground(TEXT_PRIMARY);
                    if(isRowSelected(row))c.setBackground(new Color(255,185,50,40)); return c;
                }
            };
            styleTable(table);
            table.getColumnModel().getColumn(0).setMaxWidth(50);
            table.getColumnModel().getColumn(4).setMaxWidth(70);
            table.getColumnModel().getColumn(5).setMaxWidth(110);
            table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer(){
                public Component getTableCellRendererComponent(JTable t,Object v,boolean s,boolean f,int row,int col){
                    JLabel l=(JLabel)super.getTableCellRendererComponent(t,v,s,f,row,col);
                    l.setForeground("Available".equals(v)?SUCCESS:DANGER);l.setFont(new Font("Segoe UI",Font.BOLD,12));
                    l.setBackground(row%2==0?BG_CARD:BG_ROW_ALT);l.setBorder(BorderFactory.createEmptyBorder(0,10,0,0)); return l;
                }
            });
            loadData(lib.books); addCtxMenu();
            return styledScroll(table);
        }
        void loadData(List<Book> data){ model.setRowCount(0); data.forEach(b->model.addRow(new Object[]{b.id,b.title,b.author,b.genre,b.year,b.available?"Available":"Borrowed"})); }
        void filterTable(String q){ loadData(q.isBlank()?lib.books:lib.searchBooks(q)); }
        void addCtxMenu(){
            JPopupMenu m=new JPopupMenu();m.setBackground(BG_CARD);
            JMenuItem edit=mi("Edit Book"); JMenuItem del=mi("Delete Book");
            edit.addActionListener(e->{ int row=table.getSelectedRow(); if(row<0)return; int id=(int)model.getValueAt(row,0); lib.books.stream().filter(b->b.id==id).findFirst().ifPresent(b->showEdit(b)); });
            del.addActionListener(e->{ int row=table.getSelectedRow(); if(row<0)return; int id=(int)model.getValueAt(row,0); lib.books.stream().filter(b->b.id==id).findFirst().ifPresent(b->{ if(JOptionPane.showConfirmDialog(this,"Delete \""+b.title+"\"?","Confirm",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION){lib.books.remove(b);refresh();onRefresh.run();}});});
            m.add(edit);m.add(del);
            table.addMouseListener(new MouseAdapter(){ public void mouseReleased(MouseEvent e){ if(e.isPopupTrigger()){int r=table.rowAtPoint(e.getPoint());if(r>=0)table.setRowSelectionInterval(r,r);m.show(table,e.getX(),e.getY());}}});
        }
        JMenuItem mi(String t){ JMenuItem i=new JMenuItem(t);i.setBackground(BG_CARD);i.setForeground(TEXT_PRIMARY);i.setFont(F_BODY);i.setBorder(BorderFactory.createEmptyBorder(8,16,8,16));return i; }
        void showAdd(){ BookDlg d=new BookDlg(SwingUtilities.getWindowAncestor(this),null); d.setVisible(true); if(d.result!=null){lib.books.add(d.result);refresh();onRefresh.run();} }
        void showEdit(Book b){ BookDlg d=new BookDlg(SwingUtilities.getWindowAncestor(this),b); d.setVisible(true); if(d.result!=null){refresh();onRefresh.run();} }
        public void refresh(){ loadData(lib.books); if(search!=null)search.setText(""); }
    }

    // ─────────────────────────────────────────────────────────────
    //  BOOK DIALOG
    // ─────────────────────────────────────────────────────────────
    static class BookDlg extends JDialog {
        Book result; Book existing;
        JTextField tf,af,yf; JComboBox<String> gc;
        static final String[] GENRES={"Technology","Science Fiction","Dystopian","Fantasy","Classic","History","Self-Help","Mystery","Romance","Biography","Other"};
        BookDlg(Window owner,Book ex){
            super(owner,ex==null?"Add Book":"Edit Book",ModalityType.APPLICATION_MODAL);
            existing=ex; setSize(420,400); setLocationRelativeTo(owner); setUndecorated(true); setBackground(new Color(0,0,0,0));
            JPanel root=dialogRoot(); root.setBorder(BorderFactory.createEmptyBorder(24,28,24,28));
            JLabel hdr=new JLabel(ex==null?"Add New Book":"Edit Book");hdr.setFont(new Font("Segoe UI",Font.BOLD,18));hdr.setForeground(TEXT_PRIMARY);hdr.setBorder(BorderFactory.createEmptyBorder(0,0,20,0));
            tf=styledField("Book title"); af=styledField("Author name"); yf=styledField("Year"); gc=styledCombo(GENRES);
            if(ex!=null){tf.setText(ex.title);af.setText(ex.author);yf.setText(String.valueOf(ex.year));for(int i=0;i<GENRES.length;i++)if(GENRES[i].equals(ex.genre))gc.setSelectedIndex(i);}
            JPanel form=new JPanel(new GridBagLayout());form.setOpaque(false);
            GridBagConstraints gbc=new GridBagConstraints();gbc.fill=GridBagConstraints.HORIZONTAL;gbc.weightx=1;gbc.insets=new Insets(4,0,4,0);
            gbc.gridy=0;form.add(fieldLabel("Title"),gbc);gbc.gridy=1;form.add(tf,gbc);
            gbc.gridy=2;form.add(fieldLabel("Author"),gbc);gbc.gridy=3;form.add(af,gbc);
            gbc.gridy=4;
            JPanel row=new JPanel(new GridLayout(1,2,12,0));row.setOpaque(false);
            JPanel yp=new JPanel(new BorderLayout(0,4));yp.setOpaque(false);yp.add(fieldLabel("Year"),BorderLayout.NORTH);yp.add(yf,BorderLayout.CENTER);
            JPanel gp=new JPanel(new BorderLayout(0,4));gp.setOpaque(false);gp.add(fieldLabel("Genre"),BorderLayout.NORTH);gp.add(gc,BorderLayout.CENTER);
            row.add(yp);row.add(gp);form.add(row,gbc);
            JPanel btns=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));btns.setOpaque(false);btns.setBorder(BorderFactory.createEmptyBorder(16,0,0,0));
            JButton cancel=ghostBtn("Cancel");cancel.addActionListener(e->dispose());
            JButton save=primaryBtn(ex==null?"Add Book":"Save");save.addActionListener(e->save());
            btns.add(cancel);btns.add(save);
            root.setLayout(new BorderLayout());root.add(hdr,BorderLayout.NORTH);root.add(form,BorderLayout.CENTER);root.add(btns,BorderLayout.SOUTH);
            getRootPane().setDefaultButton(save); setContentPane(root);
        }
        void save(){
            String t=tf.getText().trim(),a=af.getText().trim(),ys=yf.getText().trim();
            if(t.isEmpty()||a.isEmpty()||ys.isEmpty()){shake(this);return;}
            int y; try{y=Integer.parseInt(ys);}catch(NumberFormatException ex){shake(this);return;}
            if(existing!=null){existing.title=t;existing.author=a;existing.genre=(String)gc.getSelectedItem();existing.year=y;result=existing;}
            else result=new Book(t,a,(String)gc.getSelectedItem(),y);
            dispose();
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  MEMBERS PANEL
    // ─────────────────────────────────────────────────────────────
    static class MembersPanel extends JPanel {
        Library lib; Runnable onRefresh; DefaultTableModel model; JTable table; JTextField search;
        MembersPanel(Library l,Runnable r){lib=l;onRefresh=r;setOpaque(false);setLayout(new BorderLayout());setBorder(BorderFactory.createEmptyBorder(30,30,30,30));build();}
        void build(){ add(topBar(),BorderLayout.NORTH); add(buildTable(),BorderLayout.CENTER); }
        JPanel topBar(){
            JPanel bar=new JPanel(new BorderLayout(12,0));bar.setOpaque(false);bar.setBorder(BorderFactory.createEmptyBorder(0,0,18,0));
            JLabel t=new JLabel("Members");t.setFont(F_TITLE);t.setForeground(TEXT_PRIMARY);
            search=styledField("Search members…");search.setPreferredSize(new Dimension(240,38));
            search.addKeyListener(new KeyAdapter(){ public void keyReleased(KeyEvent e){filterTable(search.getText());}});
            JButton add=primaryBtn("+ Add Member");add.setPreferredSize(new Dimension(150,38));add.addActionListener(e->showAdd());
            JPanel right=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));right.setOpaque(false);right.add(search);right.add(add);
            bar.add(t,BorderLayout.WEST);bar.add(right,BorderLayout.EAST); return bar;
        }
        JScrollPane buildTable(){
            model=new DefaultTableModel(new String[]{"ID","Name","Email","Borrowed"},0){public boolean isCellEditable(int r,int c){return false;}};
            table=new JTable(model){
                public Component prepareRenderer(TableCellRenderer r,int row,int col){
                    Component c=super.prepareRenderer(r,row,col);c.setBackground(row%2==0?BG_CARD:BG_ROW_ALT);c.setForeground(TEXT_PRIMARY);
                    if(isRowSelected(row))c.setBackground(new Color(255,185,50,40)); return c;
                }
            };
            styleTable(table);
            table.getColumnModel().getColumn(0).setMaxWidth(50);
            table.getColumnModel().getColumn(3).setMaxWidth(100);
            table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer(){
                public Component getTableCellRendererComponent(JTable t,Object v,boolean s,boolean f,int row,int col){
                    JLabel l=(JLabel)super.getTableCellRendererComponent(t,v,s,f,row,col);
                    int cnt=v instanceof Integer?(Integer)v:0;
                    l.setForeground(cnt>0?ACCENT:TEXT_SECONDARY);l.setFont(cnt>0?new Font("Segoe UI",Font.BOLD,13):F_BODY);
                    l.setBackground(row%2==0?BG_CARD:BG_ROW_ALT);l.setBorder(BorderFactory.createEmptyBorder(0,12,0,0)); return l;
                }
            });
            loadData(lib.members); addCtxMenu();
            return styledScroll(table);
        }
        void loadData(List<Member> data){ model.setRowCount(0); data.forEach(m->model.addRow(new Object[]{m.id,m.name,m.email,m.borrowed.size()})); }
        void filterTable(String q){ loadData(q.isBlank()?lib.members:lib.searchMembers(q)); }
        Member selected(){ int row=table.getSelectedRow(); if(row<0)return null; int id=(int)model.getValueAt(row,0); return lib.members.stream().filter(m->m.id==id).findFirst().orElse(null); }
        void addCtxMenu(){
            JPopupMenu m=new JPopupMenu();m.setBackground(BG_CARD);
            JMenuItem view=mi("View Borrowed"),edit=mi("Edit Member"),del=mi("Remove Member");
            view.addActionListener(e->{ Member mm=selected(); if(mm==null)return; if(mm.borrowed.isEmpty()){JOptionPane.showMessageDialog(this,mm.name+" has no borrowed books.","Info",JOptionPane.INFORMATION_MESSAGE);return;} StringBuilder sb=new StringBuilder("<html><body style='font-family:Segoe UI;font-size:13'><b>"+mm.name+"</b> borrowed:<br><br>"); mm.borrowed.forEach(b->sb.append("• ").append(b.title).append(" — <i>").append(b.author).append("</i><br>")); sb.append("</body></html>"); JOptionPane.showMessageDialog(this,sb.toString(),"Borrowed Books",JOptionPane.INFORMATION_MESSAGE);});
            edit.addActionListener(e->{ Member mm=selected(); if(mm==null)return; MemberDlg d=new MemberDlg(SwingUtilities.getWindowAncestor(this),mm); d.setVisible(true); if(d.updated){refresh();onRefresh.run();}});
            del.addActionListener(e->{ Member mm=selected(); if(mm==null)return; if(!mm.borrowed.isEmpty()){JOptionPane.showMessageDialog(this,"Cannot remove member with borrowed books.","Error",JOptionPane.ERROR_MESSAGE);return;} if(JOptionPane.showConfirmDialog(this,"Remove "+mm.name+"?","Confirm",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION){lib.members.remove(mm);refresh();onRefresh.run();}});
            m.add(view);m.add(edit);m.add(del);
            table.addMouseListener(new MouseAdapter(){ public void mouseReleased(MouseEvent e){ if(e.isPopupTrigger()){int r=table.rowAtPoint(e.getPoint());if(r>=0)table.setRowSelectionInterval(r,r);m.show(table,e.getX(),e.getY());}}});
        }
        JMenuItem mi(String t){ JMenuItem i=new JMenuItem(t);i.setBackground(BG_CARD);i.setForeground(TEXT_PRIMARY);i.setFont(F_BODY);i.setBorder(BorderFactory.createEmptyBorder(8,16,8,16));return i; }
        void showAdd(){ MemberDlg d=new MemberDlg(SwingUtilities.getWindowAncestor(this),null); d.setVisible(true); if(d.newMember!=null){lib.members.add(d.newMember);refresh();onRefresh.run();} }
        public void refresh(){ loadData(lib.members); if(search!=null)search.setText(""); }
    }

    // ─────────────────────────────────────────────────────────────
    //  MEMBER DIALOG
    // ─────────────────────────────────────────────────────────────
    static class MemberDlg extends JDialog {
        Member newMember; Member existing; boolean updated;
        JTextField nf,ef;
        MemberDlg(Window owner,Member ex){
            super(owner,ex==null?"Add Member":"Edit Member",ModalityType.APPLICATION_MODAL);
            existing=ex; setSize(380,290); setLocationRelativeTo(owner); setUndecorated(true); setBackground(new Color(0,0,0,0));
            JPanel root=dialogRoot(); root.setBorder(BorderFactory.createEmptyBorder(24,28,24,28));
            JLabel hdr=new JLabel(ex==null?"Add Member":"Edit Member");hdr.setFont(new Font("Segoe UI",Font.BOLD,18));hdr.setForeground(TEXT_PRIMARY);hdr.setBorder(BorderFactory.createEmptyBorder(0,0,20,0));
            nf=styledField("Full name"); ef=styledField("Email address");
            if(ex!=null){nf.setText(ex.name);ef.setText(ex.email);}
            JPanel form=new JPanel(new GridBagLayout());form.setOpaque(false);
            GridBagConstraints gbc=new GridBagConstraints();gbc.fill=GridBagConstraints.HORIZONTAL;gbc.weightx=1;gbc.insets=new Insets(4,0,4,0);
            gbc.gridy=0;form.add(fieldLabel("Name"),gbc);gbc.gridy=1;form.add(nf,gbc);gbc.gridy=2;form.add(fieldLabel("Email"),gbc);gbc.gridy=3;form.add(ef,gbc);
            JPanel btns=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));btns.setOpaque(false);btns.setBorder(BorderFactory.createEmptyBorder(16,0,0,0));
            JButton cancel=ghostBtn("Cancel");cancel.addActionListener(e->dispose());
            JButton save=primaryBtn(ex==null?"Add Member":"Save");save.addActionListener(e->save());
            btns.add(cancel);btns.add(save);
            root.setLayout(new BorderLayout());root.add(hdr,BorderLayout.NORTH);root.add(form,BorderLayout.CENTER);root.add(btns,BorderLayout.SOUTH);
            getRootPane().setDefaultButton(save); setContentPane(root);
        }
        void save(){ String n=nf.getText().trim(),e=ef.getText().trim(); if(n.isEmpty()||e.isEmpty()){shake(this);return;} if(existing!=null){existing.name=n;existing.email=e;updated=true;}else newMember=new Member(n,e); dispose(); }
    }

    // ─────────────────────────────────────────────────────────────
    //  BORROW PANEL
    // ─────────────────────────────────────────────────────────────
    static class BorrowPanel extends JPanel {
        Library lib; Runnable onRefresh;
        JComboBox<Member> memberCb; JComboBox<Book> bookCb; JPanel borrowedList;
        BorrowPanel(Library l,Runnable r){lib=l;onRefresh=r;setOpaque(false);setLayout(new BorderLayout(0,0));setBorder(BorderFactory.createEmptyBorder(30,30,30,30));build();}
        void build(){
            JLabel title=new JLabel("Borrow & Return");title.setFont(F_TITLE);title.setForeground(TEXT_PRIMARY);
            JLabel sub=new JLabel("Manage book lending and returns");sub.setFont(F_BODY);sub.setForeground(TEXT_SECONDARY);
            JPanel hdr=new JPanel(new GridLayout(2,1,0,4));hdr.setOpaque(false);hdr.setBorder(BorderFactory.createEmptyBorder(0,0,20,0));
            hdr.add(title);hdr.add(sub); add(hdr,BorderLayout.NORTH);
            JPanel content=new JPanel(new GridLayout(1,2,24,0));content.setOpaque(false);
            content.add(buildBorrowCard()); content.add(buildReturnCard());
            add(content,BorderLayout.CENTER);
        }
        JPanel buildBorrowCard(){
            JPanel p=card();p.setLayout(new BorderLayout(0,16));p.setBorder(BorderFactory.createEmptyBorder(22,24,22,24));
            p.add(sectionTitle("Issue a Book"),BorderLayout.NORTH);
            memberCb=new JComboBox<>();memberCb.setBackground(BG_INPUT);memberCb.setForeground(TEXT_PRIMARY);memberCb.setFont(F_BODY);memberCb.setPreferredSize(new Dimension(0,38));
            bookCb=new JComboBox<>();bookCb.setBackground(BG_INPUT);bookCb.setForeground(TEXT_PRIMARY);bookCb.setFont(F_BODY);bookCb.setPreferredSize(new Dimension(0,38));
            lib.members.forEach(m->memberCb.addItem(m));
            lib.books.stream().filter(b->b.available).forEach(b->bookCb.addItem(b));
            memberCb.addActionListener(e->updateBorrowedList());
            JPanel form=new JPanel(new GridBagLayout());form.setOpaque(false);
            GridBagConstraints gbc=new GridBagConstraints();gbc.fill=GridBagConstraints.HORIZONTAL;gbc.weightx=1;gbc.insets=new Insets(5,0,5,0);
            gbc.gridy=0;form.add(fieldLabel("Select Member"),gbc);gbc.gridy=1;form.add(memberCb,gbc);
            gbc.gridy=2;form.add(fieldLabel("Select Book"),gbc);gbc.gridy=3;form.add(bookCb,gbc);
            JButton btn=primaryBtn("Issue Book");btn.setPreferredSize(new Dimension(0,42));btn.addActionListener(e->doIssue());
            gbc.gridy=4;gbc.insets=new Insets(14,0,0,0);form.add(btn,gbc);
            p.add(form,BorderLayout.CENTER); return p;
        }
        JPanel buildReturnCard(){
            JPanel p=card();p.setLayout(new BorderLayout(0,12));p.setBorder(BorderFactory.createEmptyBorder(22,24,22,24));
            p.add(sectionTitle("Return a Book"),BorderLayout.NORTH);
            JPanel inner=new JPanel(new BorderLayout(0,10));inner.setOpaque(false);
            inner.add(fieldLabel("Currently Borrowed"),BorderLayout.NORTH);
            borrowedList=new JPanel();borrowedList.setOpaque(false);borrowedList.setLayout(new BoxLayout(borrowedList,BoxLayout.Y_AXIS));
            updateBorrowedList();
            inner.add(styledScroll(borrowedList),BorderLayout.CENTER);
            p.add(inner,BorderLayout.CENTER); return p;
        }
        void updateBorrowedList(){
            if(borrowedList==null)return;
            borrowedList.removeAll();
            Member m=(Member)memberCb.getSelectedItem();
            if(m==null||m.borrowed.isEmpty()){JLabel e=new JLabel("No books borrowed");e.setFont(F_BODY);e.setForeground(TEXT_MUTED);e.setBorder(BorderFactory.createEmptyBorder(12,0,0,0));borrowedList.add(e);}
            else{ m.borrowed.forEach(b->{borrowedList.add(returnRow(m,b));borrowedList.add(Box.createVerticalStrut(6));});}
            borrowedList.revalidate();borrowedList.repaint();
        }
        JPanel returnRow(Member mem,Book book){
            JPanel row=new JPanel(new BorderLayout(10,0)){
                protected void paintComponent(Graphics g){Graphics2D g2=(Graphics2D)g.create();g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);g2.setColor(BG_ROW_ALT);g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);g2.dispose();}
                {setOpaque(false);setBorder(BorderFactory.createEmptyBorder(8,12,8,12));}
            };
            JLabel n=new JLabel(book.title);n.setFont(F_BODY);n.setForeground(TEXT_PRIMARY);
            JLabel a=new JLabel(book.author);a.setFont(F_SMALL);a.setForeground(TEXT_SECONDARY);
            JPanel left=new JPanel(new GridLayout(2,1));left.setOpaque(false);left.add(n);left.add(a);
            JButton ret=new JButton("Return"){
                {setOpaque(false);setContentAreaFilled(false);setBorderPainted(false);setFocusPainted(false);setFont(new Font("Segoe UI",Font.BOLD,11));setForeground(ACCENT);setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));}
                protected void paintComponent(Graphics g){Graphics2D g2=(Graphics2D)g.create();g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);g2.setColor(new Color(255,185,50,30));g2.fillRoundRect(0,0,getWidth(),getHeight(),6,6);g2.setColor(ACCENT);g2.setStroke(new BasicStroke(1f));g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,6,6);g2.dispose();super.paintComponent(g);}
            };
            ret.setPreferredSize(new Dimension(65,28));
            ret.addActionListener(e->{mem.returnBook(book);refresh();onRefresh.run();});
            row.add(left,BorderLayout.CENTER);row.add(ret,BorderLayout.EAST); return row;
        }
        void doIssue(){
            Member m=(Member)memberCb.getSelectedItem(); Book b=(Book)bookCb.getSelectedItem();
            if(m==null||b==null)return;
            if(!b.available){JOptionPane.showMessageDialog(this,"Book already borrowed.","Unavailable",JOptionPane.WARNING_MESSAGE);return;}
            m.borrow(b); refresh(); onRefresh.run();
            JOptionPane.showMessageDialog(this,"<html><b>"+b.title+"</b> issued to <b>"+m.name+"</b>.</html>","Success",JOptionPane.INFORMATION_MESSAGE);
        }
        public void refresh(){
            if(memberCb==null)return;
            Member prev=(Member)memberCb.getSelectedItem();
            memberCb.removeAllItems();lib.members.forEach(m->memberCb.addItem(m));
            if(prev!=null)memberCb.setSelectedItem(prev);
            bookCb.removeAllItems();lib.books.stream().filter(b->b.available).forEach(b->bookCb.addItem(b));
            updateBorrowedList();
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  DIALOG BACKGROUND HELPER
    // ─────────────────────────────────────────────────────────────
    static JPanel dialogRoot(){
        return new JPanel(){
            protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);g2.fillRoundRect(0,0,getWidth(),getHeight(),16,16);
                g2.setColor(BORDER);g2.setStroke(new BasicStroke(1f));g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,16,16);
                g2.dispose();
            }
            {setOpaque(false);}
        };
    }
}
