package queryprocessing;

import queryprocessing.Constant.Database;

public class ConnectionPool
{
	private static boolean conEMPLOYEEExist = false;
	private static boolean conIMDBExist = false;
	private static boolean conACMDLExist = false;
    private static SearchEngine conEMPLOYEE;
    private static SearchEngine conACMDL;
    private static SearchEngine conIMDB;
    
    private static boolean sqlConEMPLOYEEExist = false;
    private static SQLBean sqlConEMPLOYEE;
    private static boolean sqlConIMDBExist = false;
    private static SQLBean sqlConIMDB;
    private static boolean sqlConACMDLExist = false;
    private static SQLBean sqlConACMDL;
    
    public static synchronized SearchEngine getConnection(String dataset){
    	SearchEngine se = null;
    	Database db = null;
    	switch(dataset)
    	{
    		case "employee":
    			db = Database.Employee;
    			if(!conEMPLOYEEExist)
                {
                	conEMPLOYEE = new SearchEngine(db);
                    conEMPLOYEEExist = true;
                }
                if(sqlConEMPLOYEEExist && !sqlConEMPLOYEE.isValid())
                {
                	sqlConEMPLOYEE.closeDataBase();
                	sqlConEMPLOYEEExist = false;
                }
                if(!sqlConEMPLOYEEExist)
                {
                	sqlConEMPLOYEE = new SQLBean();
                	String dbname = Constant.getDBname(db);
                	sqlConEMPLOYEE.openDataBase(dbname);
//                	sqlConCOMPANY.executeQuery("set wait_timeout = 60;");
                	sqlConEMPLOYEEExist = true;
                	conEMPLOYEE.setSQLBean(sqlConEMPLOYEE);
                }
                se = conEMPLOYEE;
    			break;
    		case "imdb":
    			db = Database.IMDB;
    			if(!conIMDBExist)
                {
                	conIMDB = new SearchEngine(db);
                	conIMDBExist = true;
                }
                if(sqlConIMDBExist && !sqlConIMDB.isValid())
                {
                	sqlConIMDB.closeDataBase();
                	sqlConIMDBExist = false;
                	
                }
                if(!sqlConIMDBExist)
                {
                	sqlConIMDB = new SQLBean();
            		String dbname = Constant.getDBname(db);
            		sqlConIMDB.openDataBase(dbname);
//            		sqlConIMDB.executeQuery("set wait_timeout = 60;");
            		sqlConIMDBExist = true;
            		conIMDB.setSQLBean(sqlConIMDB);
                }
                se = conIMDB;
    			break;
    		case "acmdl": default:
    			db = Database.ACMDL;
    			if(!conACMDLExist)
                {
                	conACMDL = new SearchEngine(db);
                    conACMDLExist = true;
                }
                if(sqlConACMDLExist && !sqlConACMDL.isValid())
                {
                	sqlConACMDL.closeDataBase();
                	sqlConACMDLExist = false;
                }
                if(!sqlConACMDLExist)
                {
                	sqlConACMDL = new SQLBean();
                	String dbname = Constant.getDBname(db);
                	sqlConACMDL.openDataBase(dbname);
//                	sqlConACMDL.executeQuery("set wait_timeout = 60;");
                	sqlConACMDLExist = true;
                	conACMDL.setSQLBean(sqlConACMDL);
                }
                se = conACMDL;
    			break;
    	}
    	return se;
    }
    
    protected void finalize() throws Throwable
    {
    	if(sqlConIMDB != null)
    	{
    		sqlConIMDB.closeDataBase();
    	}
    	if(sqlConACMDL != null)
    	{
    		sqlConACMDL.closeDataBase();
    	}
    	if(sqlConEMPLOYEE != null)
    	{
    		sqlConEMPLOYEE.closeDataBase();
    	}
    	super.finalize();
    }
}
