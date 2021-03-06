package cn.org.rapid_framework.generator;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import yunnex.util.RapidUtil;
import cn.org.rapid_framework.generator.Generator.GeneratorModel;
import cn.org.rapid_framework.generator.provider.db.sql.model.Sql;
import cn.org.rapid_framework.generator.provider.db.table.TableFactory;
import cn.org.rapid_framework.generator.provider.db.table.model.Table;
import cn.org.rapid_framework.generator.provider.java.model.JavaClass;
import cn.org.rapid_framework.generator.util.BeanHelper;
import cn.org.rapid_framework.generator.util.GLogger;
import cn.org.rapid_framework.generator.util.GeneratorException;
import cn.org.rapid_framework.generator.util.typemapping.DatabaseTypeUtils;
/**
 * 
 * @author badqiu
 *
 */
public class GeneratorFacade {
	public Generator g = new Generator();
	public GeneratorFacade(){
		g.setOutRootDir(GeneratorProperties.getProperty("outRoot"));
	}
	
	public static void printAllTableNames() throws Exception {
		PrintUtils.printAllTableNames(TableFactory.getInstance().getAllTables());
	}
	
	public void deleteOutRootDir() throws IOException {
		g.deleteOutRootDir();
	}
	
	public void generateByMap(Map map,String templateRootDir) throws Exception {
		new ProcessUtils().processByMap(map, templateRootDir,false);
	}

	public void deleteByMap(Map map,String templateRootDir) throws Exception {
		new ProcessUtils().processByMap(map, templateRootDir,true);
	}
	
	public void generateByAllTable(String templateRootDir) throws Exception {
		new ProcessUtils().processByAllTable(templateRootDir,false);
	}
	
	public void deleteByAllTable(String templateRootDir) throws Exception {
		new ProcessUtils().processByAllTable(templateRootDir,true);		
	}
	
	@Deprecated
	public void generateByTables(String[] tableNames ,String templateRootDir) throws Exception {
	    generateByTables(tableNames, null, templateRootDir);
	}
	
	public void generateByTables(String[] tableNames, String moduleName, String templateRootDir) throws Exception {
	    for (String s: tableNames) {
	        new ProcessUtils().processByTable(s, moduleName, templateRootDir, false);
	    }
	}
	
	    /**
     * 
     * @param tableName
     * @param moduleName
     * @param templateRootDir
     * @throws Exception
     * @Deprecated moduleName在RapidUtil#CONFIG_PATH属性中定义，此处定义无效
     */
	@Deprecated
	public void generateByTable(String tableName, String moduleName, String templateRootDir) throws Exception {
	    new ProcessUtils().processByTable(tableName, moduleName,templateRootDir,false);
	}
	
	/**
	 * 组装生成包名的逻辑参考 RapidUtil#setTableGroupName 方法<br>
	 * RapidUtil#CONFIG_PATH中的is_soa_style属性值控制是否在包路径中使用moduleName，如果'N'则不用，为'Y' 则使用。<br>
	 * 组成生成的类的包名规则是：basepackage + modulepackage  + group_name <br>
	 * basepackage在GeneratorProperties#PROPERTIES_FILE_NAMES中定义,<br>
	 * moduleName在RapidUtil#CONFIG_PATH属性中通过modulepackage属性定义，如果没有定义值，默认值为"module"<br>
	 * groupName在RapidUtil#CONFIG_PATH属性中通过group_name属性定义,如果不想使用groupName,可以把它设置为"null",如果没有定义任何值，则默认使用表名中的首单词<br>
	 * @param tableName 数据库表名
	 * @param templateRootDir 模板目录
	 * @throws Exception
	 */
    public void generateByTable(String tableName,String templateRootDir) throws Exception {
        generateByTable(tableName, null, templateRootDir);
	}

    public void deleteByTable(String tableName,String templateRootDir) throws Exception {
    	new ProcessUtils().processByTable(tableName, null, templateRootDir,true);
	}
    
	public void generateByClass(Class clazz,String templateRootDir) throws Exception {
		new ProcessUtils().processByClass(clazz, templateRootDir,false);
	}

	public void deleteByClass(Class clazz,String templateRootDir) throws Exception {
		new ProcessUtils().processByClass(clazz, templateRootDir,true);
	}
	
	public void generateBySql(Sql sql,String templateRootDir) throws Exception {
		new ProcessUtils().processBySql(sql,templateRootDir,false);
	}

	public void deleteBySql(Sql sql,String templateRootDir) throws Exception {
		new ProcessUtils().processBySql(sql,templateRootDir,true);
	}
	
    private Generator getGenerator(String templateRootDir) {
        g.setTemplateRootDir(new File(templateRootDir).getAbsoluteFile());
        return g;
    }
    
    /** 生成器的上下文，存放的变量将可以在模板中引用 */
    public static class GeneratorContext {
        static ThreadLocal<Map> context = new ThreadLocal<Map>();
        public static void clear() {
            Map m = context.get();
            if(m != null) m.clear();
        }
        public static Map getContext() {
            Map map = context.get();
            if(map == null) {
                setContext(new HashMap());
            }
            return context.get();
        }
        public static void setContext(Map map) {
            context.set(map);
        }
        public static void put(String key,Object value) {
            getContext().put(key, value);
        }
    }
    
    public class ProcessUtils {
    	public void processByMap(Map params, String templateRootDir,boolean isDelete) throws Exception, FileNotFoundException {
			Generator g = getGenerator(templateRootDir);
			GeneratorModel m = GeneratorModelUtils.newFromMap(params);
			try {
				if(isDelete)
					g.deleteBy(m.templateModel, m.filePathModel);
				else
					g.generateBy(m.templateModel, m.filePathModel);
			}catch(GeneratorException ge) {
				PrintUtils.printExceptionsSumary(ge.getMessage(),getGenerator(templateRootDir).getOutRootDir(),ge.getExceptions());
			}
    	}
    	
    	public void processBySql(Sql sql,String templateRootDir,boolean isDelete) throws Exception {
    		Generator g = getGenerator(templateRootDir);
    		GeneratorModel m = GeneratorModelUtils.newFromSql(sql);
    		PrintUtils.printBeginProcess("sql:"+sql.getSourceSql(),isDelete);
    		try {
    			if(isDelete) {
    				g.deleteBy(m.templateModel, m.filePathModel);
    			}else {
    				g.generateBy(m.templateModel, m.filePathModel);
    			}
    		}catch(GeneratorException ge) {
    			PrintUtils.printExceptionsSumary(ge.getMessage(),getGenerator(templateRootDir).getOutRootDir(),ge.getExceptions());
    		}
    	}   
    	
    	public void processByClass(Class clazz, String templateRootDir,boolean isDelete) throws Exception, FileNotFoundException {
			Generator g = getGenerator(templateRootDir);
			GeneratorModel m = GeneratorModelUtils.newFromClass(clazz);
			PrintUtils.printBeginProcess("JavaClass:"+clazz.getSimpleName(),isDelete);
			try {
				if(isDelete)
					g.deleteBy(m.templateModel, m.filePathModel);
				else
					g.generateBy(m.templateModel, m.filePathModel);
			}catch(GeneratorException ge) {
				PrintUtils.printExceptionsSumary(ge.getMessage(),getGenerator(templateRootDir).getOutRootDir(),ge.getExceptions());
			}
    	}
    	
        public void processByTable(String tableName, String moduleName, String templateRootDir,boolean isDelete) throws Exception {
        	if("*".equals(tableName)) {
        		generateByAllTable(templateRootDir);
        		return;
        	}
    		Generator g = getGenerator(templateRootDir);
    		Table table = TableFactory.getInstance().getTable(tableName);
    		if (null != moduleName && !"".equals(moduleName)) {
    		    moduleName = "." + moduleName;
    		} else {
    		    moduleName = "";
    		}
    		table.setModuleName(moduleName.toLowerCase());
    		try {
    			processByTable(g,table,isDelete);
    		}catch(GeneratorException ge) {
    			PrintUtils.printExceptionsSumary(ge.getMessage(),getGenerator(templateRootDir).getOutRootDir(),ge.getExceptions());
    		}
    	}    
        
		public void processByAllTable(String templateRootDir,boolean isDelete) throws Exception {
			List<Table> tables = TableFactory.getInstance().getAllTables();
			List exceptions = new ArrayList();
			for(int i = 0; i < tables.size(); i++ ) {
				try {
					processByTable(getGenerator(templateRootDir),tables.get(i),isDelete);
				}catch(GeneratorException ge) {
					exceptions.addAll(ge.getExceptions());
				}
			}
			PrintUtils.printExceptionsSumary("",getGenerator(templateRootDir).getOutRootDir(),exceptions);
		}
		
		public void processByTable(Generator g, Table table,boolean isDelete) throws Exception {
		    boolean mongoFlg = RapidUtil.isMongoTable(table); //判断是否为mongoDB中的表
		    RapidUtil.setTableGroupName(table); //设置表的分组名(用于生成对应包名)
	        GeneratorModel m = GeneratorModelUtils.newFromTable(table);
	        PrintUtils.printBeginProcess(table.getSqlName()+" => "+table.getClassName(),isDelete);
	        if(isDelete)
	        	g.deleteBy(m.templateModel,m.filePathModel, mongoFlg);
	        else 
	        	g.generateBy(m.templateModel,m.filePathModel, mongoFlg);
	    }        
    }
	
    @SuppressWarnings("all")
	public static class GeneratorModelUtils {
		
		public static GeneratorModel newFromTable(Table table) {
			Map templateModel = new HashMap();
			templateModel.put("table", table);
			setShareVars(templateModel);
			
			Map filePathModel = new HashMap();
			setShareVars(filePathModel);
			filePathModel.putAll(BeanHelper.describe(table));

			return new GeneratorModel(templateModel,filePathModel);
		}

		public static GeneratorModel newFromSql(Sql sql) throws Exception {
			Map templateModel = new HashMap();
			templateModel.put("sql", sql);
			setShareVars(templateModel);
			
			Map filePathModel = new HashMap();
			setShareVars(filePathModel);
			filePathModel.putAll(BeanHelper.describe(sql));
			return new GeneratorModel(templateModel,filePathModel);
		}

		public static GeneratorModel newFromClass(Class clazz) {
			Map templateModel = new HashMap();
			templateModel.put("clazz", new JavaClass(clazz));
			setShareVars(templateModel);
			
			Map filePathModel = new HashMap();
			setShareVars(filePathModel);
			filePathModel.putAll(BeanHelper.describe(new JavaClass(clazz)));
			return new GeneratorModel(templateModel,filePathModel);
		}
		
		public static GeneratorModel newFromMap(Map params) {
			Map templateModel = new HashMap();
			templateModel.putAll(params);
			setShareVars(templateModel);
			
			Map filePathModel = new HashMap();
			setShareVars(filePathModel);
			filePathModel.putAll(params);
			return new GeneratorModel(templateModel,filePathModel);
		}
		
		public static void setShareVars(Map templateModel) {
			templateModel.putAll(GeneratorProperties.getProperties());
			templateModel.putAll(System.getProperties());
			templateModel.put("env", System.getenv());
			templateModel.put("now", new Date());
			templateModel.put("databaseType", getDatabaseType("databaseType"));
			templateModel.putAll(GeneratorContext.getContext());
		}
		
		private static String getDatabaseType(String key) {
			return GeneratorProperties.getProperty(key,DatabaseTypeUtils.getDatabaseTypeByJdbcDriver(GeneratorProperties.getProperty("jdbc.driver")));
		}

	}
	
	private static class PrintUtils {
		
		private static void printExceptionsSumary(String msg,String outRoot,List<Exception> exceptions) throws FileNotFoundException {
			File errorFile = new File(outRoot,"generator_error.log");
			if(exceptions != null && exceptions.size() > 0) {
				System.err.println("[Generate Error Summary] : "+msg);
				PrintStream output = new PrintStream(new FileOutputStream(errorFile));
				for(int i = 0; i < exceptions.size(); i++) {
					Exception e = exceptions.get(i);
                    System.err.println("[GENERATE ERROR]:"+e);
					if(i == 0) e.printStackTrace();
					e.printStackTrace(output);
				}
				output.close();
				System.err.println("***************************************************************");
				System.err.println("* "+"* 输出目录已经生成generator_error.log用于查看错误 ");
				System.err.println("***************************************************************");
			}
		}
		
		private static void printBeginProcess(String displayText,boolean isDatele) {
			GLogger.println("***************************************************************");
			GLogger.println("* BEGIN " + (isDatele ? " delete by " : " generate by ")+ displayText);
			GLogger.println("***************************************************************");
		}
		
		public static void printAllTableNames(List<Table> tables) throws Exception {
			GLogger.println("\n----All TableNames BEGIN----");
			for(int i = 0; i < tables.size(); i++ ) {
				String sqlName = ((Table)tables.get(i)).getSqlName();
				GLogger.println("g.generateTable(\""+sqlName+"\");");
			}
			GLogger.println("----All TableNames END----");
		}
	}

}
