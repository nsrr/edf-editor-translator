package viewer;

import java.awt.Color;
import java.awt.Font;
import java.util.Map;

public class SettingMcrDir extends SettingParameters{
	
	private static final long serialVersionUID = 1L;
	private static SettingMcrDir sm = null;
	
	public static SettingMcrDir setMcrDir(){
		
		if (sm==null)
			sm = new SettingMcrDir();
		
		sm.setVisible(true);
		
		return sm;
	}

	public static String getChosenDirectory(){
		
		//(1) First check system's environment variables
		String mcr_dir = getSystemMcrDir();
		if (mcr_dir!=null)
			return mcr_dir;
		
		//(2) Second check user-defined environment variables
		if (sm!=null && sm.chosenDirectory!=null){
			String text = sm.chosenDirectory.getText();
			if (text.equals(""))
				return null;
			else
				return text;
		}
		else{
			return null;
		}
	}

	private SettingMcrDir() {
		super("Setting Directory of MATLAB Compiler Runtime (MCR)");
		
		String mcr_dir = getSystemMcrDir();	
		if (mcr_dir!=null){
			chosenDirectory.setText(mcr_dir);
			chosenDirectory.setForeground(Color.BLUE);
			chosenDirectory.setFont(new Font("Serif", Font.BOLD, 14));
		}
	}
	
	private static String getSystemMcrDir(){
		Map<String, String> env = System.getenv();
		for (String envName : env.keySet()){
			if (envName.equalsIgnoreCase("path")){
//				System.out.println(env.get(envName));
				String[] vars = env.get(envName).split(";");
				for (String var : vars){
					String regex = "^.*[mM][aA][tT][lL][aA][bB] [cC][oO][mM][pP][iI][lL][eE][rR] [rR][uU][nN][tT][iI][mM][eE].*$";
					if (var.matches(regex)){
//					if (var.contains("MATLAB Compiler Runtime")){
//						System.out.println(var);
						return var;
					}
				}
				break;
			}
		}
		return null;
	}
}
