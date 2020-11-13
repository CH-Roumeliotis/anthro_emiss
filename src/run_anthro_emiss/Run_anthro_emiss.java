package run_anthro_emiss;

import java.io.File;
import java.io.IOException;
import java.io.*;

public class Run_anthro_emiss {

    
    public static void main(String[] args) {
        
        String START_YYYY; 
        String START_MM;
        String START_DD;
        
        
        File source = new File("/opt/intel/compilers_and_libraries_2017/linux/mpi/bin64/mpivars.sh -arch intel64");
        
        //Clean up EDGAR_HTAP existing files from working directory
        //rm EDGAR_HTAP*
        File file = new File("EDGAR_HTAP");
        file.delete();
        if(file.delete()){
            System.out.println("EDGAR_HTAP File deleted from Project directory\n");
        }else System.out.println("EDGAR_HTAP doesn't exist in the project directory\n");
        
        //Get script arguments
        String START_DATE = args[1];
        String START_HH = args[2];
        String RUN_TIME = args[3];
        String SCEN = args[4]; //SCEN=0 Baseline / SCEN=1 One of Julia's scenarios
        String AQM = args[5];  //AQM=0 for WRF-Chem / AQM=1 for CAMx
        String RUN_GRID = args[6];
        String SCEN_FILE = args[7];
        
        System.out.println("START_DATE is: " + START_DATE);
        System.out.println("START_HH is: " + START_HH);
        System.out.println("RUN_TIME is: " + RUN_TIME);
        System.out.println("SCEN is: " + SCEN);
        System.out.println("AQM is: " + AQM);
        System.out.println("================");
        
        
        
        //########## START DATE ##########
        //################################
        
        START_YYYY = START_DATE.substring(0, 4);
        START_MM = START_DATE.substring(5, 6);
        START_DD = START_DATE.substring(7, 8);
        
        File ANTHRODIR = new File("/hdd4/gsakelaris/run_anthro");
        
        System.out.println("\nANTHRODIR is: " + ANTHRODIR.getAbsolutePath());
        
        
        
        //########## CHOOSE RUN YEAR FOR EMISSIONS ##########
        //###################################################
        
        System.out.println("\nSTART_YYYY is initially: " + START_YYYY);
        
        int i = Integer.parseInt(START_YYYY); 
        
        if(i >= 2030){
            START_YYYY="2030";
            System.out.println("START_YYYY now is: "+ START_YYYY);
            if(ANTHRODIR.isFile()){
                
                File destDir = new File(ANTHRODIR.getAbsolutePath());
                File srcFile = new File("./anthro_" + START_YYYY);
                try{
                EmissInvCopy.copyFile(srcFile,destDir);
                }catch (IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("\nCopied all EDGAR_HTAP files from anthro_"+ START_YYYY + " in " + ANTHRODIR.getAbsolutePath());      
        }
        else if(i >= 2020 && i < 2030){
            START_YYYY="2020";
            System.out.println("START_YYYY NOW is: "+ START_YYYY);
            if(ANTHRODIR.isFile()){
                
                File destDir = new File(ANTHRODIR.getAbsolutePath());
                File srcFile = new File("./anthro_" + START_YYYY);
                try{
                EmissInvCopy.copyFile(srcFile,destDir);
                }catch (IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("\nCopied all EDGAR_HTAP files from anthro_" + START_YYYY + " in " + ANTHRODIR.getAbsolutePath());
        }
        else{
            START_YYYY="2015";
            System.out.println("START_YYYY NOW is: " + START_YYYY);
            
            if(ANTHRODIR.isFile()){
                
                File destDir = new File(ANTHRODIR.getAbsolutePath());
                File srcFile = new File("./anthro_" + START_YYYY);
                try{
                EmissInvCopy.copyFile(srcFile,destDir);
                }catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //cp ./anthro_$START_YYYY/* .
            System.out.println("\nCopied all EDGAR_HTAP files from anthro_" + START_YYYY + " in " + ANTHRODIR.getAbsolutePath());
        }
        
        
        
        //#################################################################
        //############################ WRF-Chem ###########################
        
        if(AQM.equals("0")){
            System.out.println("\nAQM=0 and if statement is executed\n");
        
            File WRFDIR = new File("/hdd4/gsakelaris/run_anthro/WRFchemi_" + START_YYYY);
            if(!WRFDIR.isDirectory()){
                WRFDIR.mkdir();
            }
        
        
        //If scenario is applied, update EDGAR_HTAP_USTUTT_emi files for all pollutants
        
        if(SCEN.equals("1")){
            Runtime rt = Runtime.getRuntime();
            String[] commands22 = {"python3.7", "bottomup_all_pp.py" +START_YYYY, "" +SCEN_FILE};
            Process proc = null;
            try {
            proc = rt.exec(commands22);
            } catch (IOException e) {
            e.printStackTrace();
            }
            //python3.7 bottomup_all_pp.py $START_YYYY $SCEN_FILE
            System.out.println("Scenario case is applied");
            System.out.println("EDGAR_HTAP files were updated with Bottom-up files\n");
        }
        
        System.out.println("Starting anthro_emis\n");
        
        
        //Run anthro_emis for all sectors
        
        try(PrintWriter out = new PrintWriter("RADM2_SORGAM_Agriculture.inp"))
        {
            String RADM2_SORGAM_Agriculture ="&CONTROL\n"+
                    "anthro_dir = " + ANTHRODIR.getAbsolutePath() + "\n"+
                    "wrf_dir    = '/hdd4/gsakelaris/test_nc/wrf_files'\n"+
                    "src_file_prefix = 'EDGAR_HTAP_USTUTT_emi_'\n"+
                    "src_file_suffix = '_" + START_YYYY +".0.0085x0.0085_0001.nc'\n"+
                    "src_names = 'CO(28)','SO2(64)','NH3(17)','NOx(33.49456868)','NMVOC(59.384926)','PM2.5(1)','PM10(1)'\n"+
                    "sub_categories  = 'Agriculture','Energy','Industry','Residential','Transport'\n"+
                    "cat_var_prefix  = ' '\n"+
                    "serial_output   = .true.\n"+
                    "start_output_time = '" + START_YYYY +"-01-01_00:00:00'\n"+
                    "stop_output_time = '" + START_YYYY +"-01-01_00:00:00'\n"+
                    "output_interval = 3600\n"+
                    "data_yrs_offset   = 0\n"+
                    "domains = 2\n"+
                    "emissions_zdim_stag = 1\n"+
                    "emis_map = 'CO->CO(Agriculture)','SO2->SO2(Agriculture)','NH3->NH3(Agriculture)',\n"+
                    "	'NO->0.7815342*NOx(Agriculture)','NO2->0.21844658*NOx(Agriculture)',\n"+
                    "	'ISO->0.0*NMVOC(Agriculture)','ETH->0.12055*NMVOC(Agriculture)','HC3->0.38067*NMVOC(Agriculture)',\n"+
                    "	'HC5->0.18505*NMVOC(Agriculture)','HC8->0.07905*NMVOC(Agriculture)','OL2->0.0*NMVOC(Agriculture)',\n"+
                    "	'OLT->0.045723*NMVOC(Agriculture)','OLI->0.0*NMVOC(Agriculture)','TOL->0.04826*NMVOC(Agriculture)',\n"+
                    "	'XYL->0.05372*NMVOC(Agriculture)','CSL->0.0*NMVOC(Agriculture)','HCHO->0.01282*NMVOC(Agriculture)',\n"+
                    "	'ALD->0.02798*NMVOC(Agriculture)','KET->0.02031*NMVOC(Agriculture)','ORA2->0.02585*NMVOC(Agriculture)',\n"+
                    "	'PM25I(a)->0.2*PM2.5(Agriculture)','PM25J(a)->0.8*PM2.5(Agriculture)','PM_10(a)->PM10(Agriculture)',\n"+
                    "	'ECI(a)->0.0*CO(Agriculture)','ECJ(a)->0.0*CO(Agriculture)',\n"+
                    "	'ORGI(a)->0.0*CO(Agriculture)','ORGJ(a)->0.0*CO(Agriculture)',\n"+
                    "	'SO4I(a)->0.0*PM10(Agriculture)','SO4J(a)->0.0*PM10(Agriculture)',\n"+
                    "	'NO3I(a)->0.0*PM10(Agriculture)','NO3J(a)->0.0*PM10(Agriculture)',\n"+
                    "	'NAAI(a)->0.0*PM10(Agriculture)','NAAJ(a)->0.0*PM10(Agriculture)',\n"+
                    "	'ORGI_A(a)->0.0*PM10(Agriculture)','ORGJ_A(a)->0.0*PM10(Agriculture)',\n"+
                    "	'ORGI_BB(a)->0.0*PM10(Agriculture)','ORGJ_BB(a)->0.0*PM10(Agriculture)'\n"+
                    "/\n";
            out.println(RADM2_SORGAM_Agriculture);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        
        
        System.out.println("Changes in RADM2_SORGAM_Agriculture.inp were made");
        System.out.println("Start run of anthro_emis");
        
        Runtime rt = Runtime.getRuntime();
        String[] commands = {"/bin/bash", "./anthro_emis < RADM2_SORGAM_Agriculture.inp"};
        Process proc = null;
        try {
            proc = rt.exec(commands);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        File f1 = new File("wrfchemi_d01");
        File f2 = new File("wrfchemi_d01_" + START_YYYY + "-" + START_MM + "-" + START_DD+ "_00:00:00_Agriculture");
        f1.renameTo(f2);
        
        File f3 = new File("wrfchemi_d02");
        File f4 = new File("WRFDIR/wrfchemi_d02_" + START_YYYY + "-" + START_MM + "-" + START_DD+ "_00:00:00_Agriculture");
        f3.renameTo(f4);
        
        try(PrintWriter out = new PrintWriter(ANTHRODIR.getAbsolutePath()+"/RADM2_SORGAM_Energy.inp")){
            
            String RADM2_SORGAM_Energy = "&CONTROL\n"+
                    "anthro_dir = " + ANTHRODIR.getAbsolutePath() + "\n"+
                    "wrf_dir    = '/hdd4/gsakelaris/test_nc/wrf_files'\n"+
                    "src_file_prefix = 'EDGAR_HTAP_USTUTT_emi_'\n"+
                    "src_file_suffix = '_" + START_YYYY +".0.0085x0.0085_0001.nc'\n"+
                    "src_names = 'CO(28)','SO2(64)','NH3(17)','NOx(33.49456868)','NMVOC(59.384926)','PM2.5(1)','PM10(1)'\n"+
                    "sub_categories  = 'Agriculture','Energy','Industry','Residential','Transport'\n"+
                    "cat_var_prefix  = ' '\n"+
                    "serial_output   = .true.\n"+
                    "start_output_time = '" + START_YYYY +"-01-01_00:00:00'\n"+
                    "stop_output_time = '" + START_YYYY +"-01-01_00:00:00'\n"+
                    "output_interval = 3600\n"+
                    "data_yrs_offset   = 0\n"+
                    "domains = 2\n"+
                    "emissions_zdim_stag = 1\n"+
                    "emis_map = 'CO->CO(Energy)','SO2->SO2(Energy)','NH3->NH3(Energy)',\n"+
                    "	'NO->0.7815342*NOx(Energy)','NO2->0.21844658*NOx(Energy)',\n"+
                    "	'ISO->0.0*NMVOC(Energy)','ETH->0.12055*NMVOC(Energy)','HC3->0.38067*NMVOC(Energy)',\n"+
                    "	'HC5->0.18505*NMVOC(Energy)','HC8->0.07905*NMVOC(Energy)','OL2->0.0*NMVOC(Energy)',\n"+
                    "	'OLT->0.045723*NMVOC(Energy)','OLI->0.0*NMVOC(Energy)','TOL->0.04826*NMVOC(Energy)',\n"+
                    "	'XYL->0.05372*NMVOC(Energy)','CSL->0.0*NMVOC(Energy)','HCHO->0.01282*NMVOC(Energy)',\n"+
                    "	'ALD->0.02798*NMVOC(Energy)','KET->0.02031*NMVOC(Energy)','ORA2->0.02585*NMVOC(Energy)',\n"+
                    "	'PM25I(a)->0.2*PM2.5(Energy)','PM25J(a)->0.8*PM2.5(Energy)','PM_10(a)->PM10(Energy)',\n"+
                    "	'ECI(a)->0.0*CO(Energy)','ECJ(a)->0.0*CO(Energy)',\n"+
                    "	'ORGI(a)->0.0*CO(Energy)','ORGJ(a)->0.0*CO(Energy)',\n"+
                    "	'SO4I(a)->0.0*PM10(Energy)','SO4J(a)->0.0*PM10(Energy)',\n"+
                    "	'NO3I(a)->0.0*PM10(Energy)','NO3J(a)->0.0*PM10(Energy)',\n"+
                    "	'NAAI(a)->0.0*PM10(Energy)','NAAJ(a)->0.0*PM10(Energy)',\n"+
                    "	'ORGI_A(a)->0.0*PM10(Energy)','ORGJ_A(a)->0.0*PM10(Energy)',\n"+
                    "	'ORGI_BB(a)->0.0*PM10(Energy)','ORGJ_BB(a)->0.0*PM10(Energy)'\n"+
                    "/\n";
            out.println(RADM2_SORGAM_Energy);
        }catch(FileNotFoundException e){
            
            e.printStackTrace();
        }
        
        System.out.println("Changes in RADM2_SORGAM_Energy.inp were made");
        System.out.println("Start run of anthro_emis");
        
        String[] commands1 = {"/bin/bash", "./anthro_emis < RADM2_SORGAM_Energy.inp"};
        Process proc1 = null;
        try {
            proc1 = rt.exec(commands1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        File f5 = new File("wrfchemi_d01");
        File f6 = new File("WRFDIR/wrfchemi_d01_" + START_YYYY + "-" + START_MM + "-" + START_DD+ "_00:00:00_Energy");
        f5.renameTo(f6);
        
        File f7 = new File("wrfchemi_d02");
        File f8 = new File("WRFDIR/wrfchemi_d02_" + START_YYYY + "-" + START_MM + "-" + START_DD+ "_00:00:00_Energy");
        f7.renameTo(f8);
        
        try(PrintWriter out = new PrintWriter(ANTHRODIR.getAbsolutePath()+"/RADM2_SORGAM_Industry.inp")){
            
            String RADM2_SORGAM_Industry = "&CONTROL\n"+
                    "anthro_dir = " + ANTHRODIR.getAbsolutePath() + "\n"+
                    "wrf_dir    = '/hdd4/gsakelaris/test_nc/wrf_files'\n"+
                    "src_file_prefix = 'EDGAR_HTAP_USTUTT_emi_'\n"+
                    "src_file_suffix = '_" + START_YYYY +".0.0085x0.0085_0001.nc'\n"+
                    "src_names = 'CO(28)','SO2(64)','NH3(17)','NOx(33.49456868)','NMVOC(59.384926)','PM2.5(1)','PM10(1)'\n"+
                    "sub_categories  = 'Agriculture','Energy','Industry','Residential','Transport'\n"+
                    "cat_var_prefix  = ' '\n"+
                    "serial_output   = .true.\n"+
                    "start_output_time = '" + START_YYYY +"-01-01_00:00:00'\n"+
                    "stop_output_time = '" + START_YYYY +"-01-01_00:00:00'\n"+
                    "output_interval = 3600\n"+
                    "data_yrs_offset   = 0\n"+
                    "domains = 2\n"+
                    "emissions_zdim_stag = 1\n"+
                    "emis_map = 'CO->CO(Industry)','SO2->SO2(Industry)','NH3->NH3(Industry)',\n"+
                    "	'NO->0.7815342*NOx(Industry)','NO2->0.21844658*NOx(Industry)',\n"+
                    "	'ISO->0.0*NMVOC(Industry)','ETH->0.12055*NMVOC(Industry)','HC3->0.38067*NMVOC(Industry)',\n"+
                    "	'HC5->0.18505*NMVOC(Industry)','HC8->0.07905*NMVOC(Industry)','OL2->0.0*NMVOC(Industry)',\n"+
                    "	'OLT->0.045723*NMVOC(Industry)','OLI->0.0*NMVOC(Industry)','TOL->0.04826*NMVOC(Industry)',\n"+
                    "	'XYL->0.05372*NMVOC(Industry)','CSL->0.0*NMVOC(Industry)','HCHO->0.01282*NMVOC(Industry)',\n"+
                    "	'ALD->0.02798*NMVOC(Industry)','KET->0.02031*NMVOC(Industry)','ORA2->0.02585*NMVOC(Industry)',\n"+
                    "	'PM25I(a)->0.2*PM2.5(Industry)','PM25J(a)->0.8*PM2.5(Industry)','PM_10(a)->PM10(Industry)',\n"+
                    "	'ECI(a)->0.0*CO(Industry)','ECJ(a)->0.0*CO(Industry)',\n"+
                    "	'ORGI(a)->0.0*CO(Industry)','ORGJ(a)->0.0*CO(Industry)',\n"+
                    "	'SO4I(a)->0.0*PM10(Industry)','SO4J(a)->0.0*PM10(Industry)',\n"+
                    "	'NO3I(a)->0.0*PM10(Industry)','NO3J(a)->0.0*PM10(Industry)',\n"+
                    "	'NAAI(a)->0.0*PM10(Industry)','NAAJ(a)->0.0*PM10(Industry)',\n"+
                    "	'ORGI_A(a)->0.0*PM10(Industry)','ORGJ_A(a)->0.0*PM10(Industry)',\n"+
                    "	'ORGI_BB(a)->0.0*PM10(Industry)','ORGJ_BB(a)->0.0*PM10(Industry)'\n"+
                    "/\n";
            out.println(RADM2_SORGAM_Industry);
        }catch(FileNotFoundException e){
            
            e.printStackTrace();
        }
        
        System.out.println("Changes in RADM2_SORGAM_Industry.inp were made");
        System.out.println("Start run of anthro_emis");
        
        String[] commands2 = {"/bin/bash", "./anthro_emis < RADM2_SORGAM_Industry.inp"};
        Process proc2 = null;
        try {
            proc2 = rt.exec(commands2);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        File f9 = new File("wrfchemi_d01");
        File f10 = new File("WRFDIR/wrfchemi_d01_" + START_YYYY + "-" + START_MM + "-" + START_DD+ "_00:00:00_Industry");
        f9.renameTo(f10);
        
        File f11 = new File("wrfchemi_d02");
        File f12 = new File("WRFDIR/wrfchemi_d02_" + START_YYYY + "-" + START_MM + "-" + START_DD+ "_00:00:00_Industry");
        f11.renameTo(f12);        
        
        try(PrintWriter out = new PrintWriter(ANTHRODIR.getAbsolutePath()+"/RADM2_SORGAM_Transport.inp")){
            
            String RADM2_SORGAM_Transport = "&CONTROL\n"+
                    "anthro_dir = " + ANTHRODIR.getAbsolutePath() + "\n"+
                    "wrf_dir    = '/hdd4/gsakelaris/test_nc/wrf_files'\n"+
                    "src_file_prefix = 'EDGAR_HTAP_USTUTT_emi_'\n"+
                    "src_file_suffix = '_" + START_YYYY +".0.0085x0.0085_0001.nc'\n"+
                    "src_names = 'CO(28)','SO2(64)','NH3(17)','NOx(33.49456868)','NMVOC(59.384926)','PM2.5(1)','PM10(1)'\n"+
                    "sub_categories  = 'Agriculture','Energy','Industry','Residential','Transport'\n"+
                    "cat_var_prefix  = ' '\n"+
                    "serial_output   = .true.\n"+
                    "start_output_time = '" + START_YYYY +"-01-01_00:00:00'\n"+
                    "stop_output_time = '" + START_YYYY +"-01-01_00:00:00'\n"+
                    "output_interval = 3600\n"+
                    "data_yrs_offset   = 0\n"+
                    "domains = 2\n"+
                    "emissions_zdim_stag = 1\n"+
                    "emis_map = 'CO->CO(Transport)','SO2->SO2(Transport)','NH3->NH3(Transport)',\n"+
                    "	'NO->0.7815342*NOx(Transport)','NO2->0.21844658*NOx(Transport)',\n"+
                    "	'ISO->0.0*NMVOC(Transport)','ETH->0.12055*NMVOC(Transport)','HC3->0.38067*NMVOC(Transport)',\n"+
                    "	'HC5->0.18505*NMVOC(Transport)','HC8->0.07905*NMVOC(Transport)','OL2->0.0*NMVOC(Transport)',\n"+
                    "	'OLT->0.045723*NMVOC(Transport)','OLI->0.0*NMVOC(Transport)','TOL->0.04826*NMVOC(Transport)',\n"+
                    "	'XYL->0.05372*NMVOC(Transport)','CSL->0.0*NMVOC(Transport)','HCHO->0.01282*NMVOC(Transport)',\n"+
                    "	'ALD->0.02798*NMVOC(Transport)','KET->0.02031*NMVOC(Transport)','ORA2->0.02585*NMVOC(Transport)',\n"+
                    "	'PM25I(a)->0.2*PM2.5(Transport)','PM25J(a)->0.8*PM2.5(Transport)','PM_10(a)->PM10(Transport)',\n"+
                    "	'ECI(a)->0.0*CO(Transport)','ECJ(a)->0.0*CO(Transport)',\n"+
                    "	'ORGI(a)->0.0*CO(Transport)','ORGJ(a)->0.0*CO(Transport)',\n"+
                    "	'SO4I(a)->0.0*PM10(Transport)','SO4J(a)->0.0*PM10(Transport)',\n"+
                    "	'NO3I(a)->0.0*PM10(Transport)','NO3J(a)->0.0*PM10(Transport)',\n"+
                    "	'NAAI(a)->0.0*PM10(Transport)','NAAJ(a)->0.0*PM10(Transport)',\n"+
                    "	'ORGI_A(a)->0.0*PM10(Transport)','ORGJ_A(a)->0.0*PM10(Transport)',\n"+
                    "	'ORGI_BB(a)->0.0*PM10(Transport)','ORGJ_BB(a)->0.0*PM10(Transport)'\n"+
                    "/\n";
            out.println(RADM2_SORGAM_Transport);
        }catch(FileNotFoundException e){
            
            e.printStackTrace();
        }
        
        System.out.println("Changes in RADM2_SORGAM_Transport.inp were made");
        System.out.println("Start run of anthro_emis");
        
        String[] commands3 = {"/bin/bash", "./anthro_emis < RADM2_SORGAM_Transport.inp"};
        Process proc3 = null;
        try {
            proc3 = rt.exec(commands3);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        File f13 = new File("wrfchemi_d01");
        File f14 = new File("WRFDIR/wrfchemi_d01_" + START_YYYY + "-" + START_MM + "-" + START_DD+ "_00:00:00_Transport");
        f13.renameTo(f14);
        
        File f15 = new File("wrfchemi_d02");
        File f16 = new File("WRFDIR/wrfchemi_d02_" + START_YYYY + "-" + START_MM + "-" + START_DD+ "_00:00:00_Transport");
        f15.renameTo(f16);
        
        try(PrintWriter out = new PrintWriter(ANTHRODIR.getAbsolutePath()+"/RADM2_SORGAM_Residential.inp")){
            
            String RADM2_SORGAM_Residential = "&CONTROL\n"+
                    "anthro_dir = " + ANTHRODIR.getAbsolutePath() + "\n"+
                    "wrf_dir    = '/hdd4/gsakelaris/test_nc/wrf_files'\n"+
                    "src_file_prefix = 'EDGAR_HTAP_USTUTT_emi_'\n"+
                    "src_file_suffix = '_" + START_YYYY +".0.0085x0.0085_0001.nc'\n"+
                    "src_names = 'CO(28)','SO2(64)','NH3(17)','NOx(33.49456868)','NMVOC(59.384926)','PM2.5(1)','PM10(1)'\n"+
                    "sub_categories  = 'Agriculture','Energy','Industry','Residential','Transport'\n"+
                    "cat_var_prefix  = ' '\n"+
                    "serial_output   = .true.\n"+
                    "start_output_time = '" + START_YYYY +"-01-01_00:00:00'\n"+
                    "stop_output_time = '" + START_YYYY +"-01-01_00:00:00'\n"+
                    "output_interval = 3600\n"+
                    "data_yrs_offset   = 0\n"+
                    "domains = 2\n"+
                    "emissions_zdim_stag = 1\n"+
                    "emis_map = 'CO->CO(Residential)','SO2->SO2(Residential)','NH3->NH3(Residential)',\n"+
                    "	'NO->0.7815342*NOx(Residential)','NO2->0.21844658*NOx(Residential)',\n"+
                    "	'ISO->0.0*NMVOC(Residential)','ETH->0.12055*NMVOC(Residential)','HC3->0.38067*NMVOC(Residential)',\n"+
                    "	'HC5->0.18505*NMVOC(Residential)','HC8->0.07905*NMVOC(Residential)','OL2->0.0*NMVOC(Residential)',\n"+
                    "	'OLT->0.045723*NMVOC(Residential)','OLI->0.0*NMVOC(Residential)','TOL->0.04826*NMVOC(Residential)',\n"+
                    "	'XYL->0.05372*NMVOC(Residential)','CSL->0.0*NMVOC(Residential)','HCHO->0.01282*NMVOC(Residential)',\n"+
                    "	'ALD->0.02798*NMVOC(Residential)','KET->0.02031*NMVOC(Residential)','ORA2->0.02585*NMVOC(Residential)',\n"+
                    "	'PM25I(a)->0.2*PM2.5(Residential)','PM25J(a)->0.8*PM2.5(Residential)','PM_10(a)->PM10(Residential)',\n"+
                    "	'ECI(a)->0.0*CO(Residential)','ECJ(a)->0.0*CO(Residential)',\n"+
                    "	'ORGI(a)->0.0*CO(Residential)','ORGJ(a)->0.0*CO(Residential)',\n"+
                    "	'SO4I(a)->0.0*PM10(Residential)','SO4J(a)->0.0*PM10(Residential)',\n"+
                    "	'NO3I(a)->0.0*PM10(Residential)','NO3J(a)->0.0*PM10(Residential)',\n"+
                    "	'NAAI(a)->0.0*PM10(Residential)','NAAJ(a)->0.0*PM10(Residential)',\n"+
                    "	'ORGI_A(a)->0.0*PM10(Residential)','ORGJ_A(a)->0.0*PM10(Residential)',\n"+
                    "	'ORGI_BB(a)->0.0*PM10(Residential)','ORGJ_BB(a)->0.0*PM10(Residential)'\n"+
                    "/\n";
            out.println(RADM2_SORGAM_Residential);
        }catch(FileNotFoundException e){
            
            e.printStackTrace();
        }  
        
        System.out.println("Changes in RADM2_SORGAM_Residential.inp were made");
        System.out.println("Start run of anthro_emis");
        
        
        String[] commands4 = {"/bin/bash", "./anthro_emis < RADM2_SORGAM_Residential.inp"};
        Process proc4 = null;
        try {
            proc4 = rt.exec(commands4);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        
        //cd $WRFDIR
        //cp ../wrfchemi_d01* .
        //cp ../wrfchemi_d02* .
        
        
        if(WRFDIR.isFile()){
                
            File dest = new File(WRFDIR.getAbsolutePath());
            File src = new File("../wrfchemi_d01");
            try{
            EmissInvCopy.copyFile(src,dest);
            }catch (IOException e) {
                e.printStackTrace();
            }
          }
        
        if(WRFDIR.isFile()){
                
            File dest1 = new File(WRFDIR.getAbsolutePath());
            File src1 = new File("../wrfchemi_d02");
            try{
            EmissInvCopy.copyFile(src1,dest1);
            }catch (IOException e) {
                e.printStackTrace();
            }
          }
        
        File f17 = new File("wrfchemi_d01");
        File f18 = new File("WRFDIR/wrfchemi_d01_" + START_YYYY + "-" + START_MM + "-" + START_DD+ "_00:00:00_Residential");
        f17.renameTo(f18);
        
        File f19 = new File("wrfchemi_d02");
        File f20 = new File("WRFDIR/wrfchemi_d02_" + START_YYYY + "-" + START_MM + "-" + START_DD+ "_00:00:00_Residential");
        f19.renameTo(f20);
        
        //Anthro emiss procedure finished! All wrfchemi files are ready.
        //Temporal profiles procedure follows.
        //Copy wrfchemi files to new dir for safety.
        
        File wrfchemi_ini = new File("wrfchemi_ini");
        if(wrfchemi_ini.isDirectory()){
            wrfchemi_ini.mkdir();
        }
            
        //cd wrfchemi_ini
        //rm *
        //cp ../wrfchemi_d* . 
        
        if(wrfchemi_ini.isFile()){
            File destDir1 = new File(wrfchemi_ini.getAbsolutePath());
            File srcFile1 = new File("../wrfchemi_d*");
            try{
            EmissInvCopy.copyFile(srcFile1, destDir1);
            }catch (IOException e) {
                e.printStackTrace();
            }
          }
        
        
        //Return to $WRFDIR and apply the temporal profiles there
        
        //cd $WRFDIR
        //cp ../Coef_calc_wrfchemi.py .   
        
        if(WRFDIR.isFile()){
            
            File dest2 = new File(WRFDIR.getAbsolutePath());
            File src2 = new File("../Coef_calc_wrfchemi.py");
            try{
            EmissInvCopy.copyFile(src2,dest2);
            }catch (IOException e) {
                e.printStackTrace();
            }
          }
        
            String[] cmnds = {"python", "Coef_calc_wrfchemi.py" +START_YYYY, "" +START_MM, "" +START_DD, "d01"};
            Process procc = null;
            try {
            procc = rt.exec(cmnds);
            } catch (IOException e) {
            e.printStackTrace();
            }
        //python Coef_calc_wrfchemi.py $START_YYYY $START_MM $START_DD d01
        
        
            String[] cmnds1 = {"python", "Coef_calc_wrfchemi.py" +START_YYYY, "" +START_MM, "" +START_DD, "" +RUN_GRID};
            Process procc1 = null;
            try {
            procc1 = rt.exec(cmnds1);
            } catch (IOException e) {
            e.printStackTrace();
            }
        //python Coef_calc_wrfchemi.py $START_YYYY $START_MM $START_DD $RUN_GRID
        }
        //#############################################################
        //############################ CAMx ###########################
        else if(AQM.equals("1")){
            File CAMxDIR = new File("/hdd4/gsakelaris/run_anthro/CAMx_wrfchemi_"+START_YYYY);
            if(CAMxDIR.isDirectory()){
                CAMxDIR.mkdir();
            }
            
            //If scenario is applied, update EDGAR_HTAP_USTUTT_emi files for all pollutants
            
            if(SCEN.equals("1")){
                //python bottomup_all_pp.py $START_YYYY $SCEN_FILE
                Runtime rt = Runtime.getRuntime();
                String[] cmnds1 = {"python", "bottomup_all_pp.py" +START_YYYY, "" +SCEN_FILE};
                Process procc1 = null;
                try {
                procc1 = rt.exec(cmnds1);
                } catch (IOException e) {
                e.printStackTrace();
                }
                System.out.println("Scenario case is applied");
                System.out.println("EDGAR_HTAP files were updated with Bottom-up files");
            }
            
             System.out.println("\nStarting anthro_emiss ");
            
            try(PrintWriter out = new PrintWriter(ANTHRODIR.getAbsolutePath()+"/CB6_r4_Agriculture.inp")){
            
            String CB6_r4_Agriculture = "&CONTROL\n"+
                    "anthro_dir = " + ANTHRODIR.getAbsolutePath() + "\n"+
                    "wrf_dir    = '/hdd4/gsakelaris/test_nc/wrf_files'\n"+
                    "src_file_prefix = 'EDGAR_HTAP_USTUTT_emi_'\n"+
                    "src_file_suffix = '_" + START_YYYY +".0.0085x0.0085_0001.nc'\n"+
                    "src_names = 'CO(28)','SO2(64)','NH3(17)','NOx(33.49456868)','NMVOC(59.384926)','PM2.5(1)','PM10(1)'\n"+
                    "sub_categories  = 'Agriculture','Energy','Industry','Residential','Transport'\n"+
                    "cat_var_prefix  = ' '\n"+
                    "serial_output   = .true.\n"+
                    "start_output_time = '" + START_YYYY +"-01-01_00:00:00'\n"+
                    "stop_output_time = '" + START_YYYY +"-01-01_00:00:00'\n"+
                    "output_interval = 3600\n"+
                    "data_yrs_offset   = 0\n"+
                    "domains = 2\n"+
                    "emissions_zdim_stag = 1\n"+
                    "emis_map = 'CO->CO(Agriculture)','SO2->SO2(Agriculture)','NH3->NH3(Agriculture)','CH4->0.079447322970639*NMVOC(Agriculture)',\n"+
                    "   'NO->0.9*NOx(Agriculture)','NO2->0.1*NOx(Agriculture)',\n"+
                    "   'ECH4->0.0*CO(Agriculture)','HONO->0.0*NH3(Agriculture)','SULF->0.0*SO2(Agriculture)','CL2->0.0*SO2(Agriculture)',\n"+
                    "   'ACET->0.0158678756476684*NMVOC(Agriculture)','ALD2->0.00963773747841105*NMVOC(Agriculture)','ALDX->0.0154360967184801*NMVOC(Agriculture)',\n"+
                    "	'BENZ->0.030440414507772*NMVOC(Agriculture)','ETH->0.0308721934369603*NMVOC(Agriculture)','ETHA->0.0150043177892919*NMVOC(Agriculture)',\n"+
                    "	'ETHY->0.0092832469775475*NMVOC(Agriculture)','ETOH->0.0146804835924007*NMVOC(Agriculture)','FORM->0.0167314335060449*NMVOC(Agriculture)',\n"+
                    "	'IOLE->0.0130613126079447*NMVOC(Agriculture)','ISOP->0.00431757340241796*NMVOC(Agriculture)','IVOC->0.121545768566494*NMVOC(Agriculture)',\n"+
                    "	'KET->0.0061987262521589*NMVOC(Agriculture)','MEOH->0.0161917098445596*NMVOC(Agriculture)','NVOL->0.00496470207253886*NMVOC(Agriculture)',\n"+
                    "	'OLE->0.0530008635578584*NMVOC(Agriculture)','PAR->0.350388601036269*NMVOC(Agriculture)','PRPA->0.0259067357512953*NMVOC(Agriculture)',\n"+
                    "	'TERP->0.00712964162348877*NMVOC(Agriculture)','TOL->0.053972366148532*NMVOC(Agriculture)','UNR->0.149287564766839*NMVOC(Agriculture)',\n"+
                    "	'XYL->0.0361614853195164*NMVOC(Agriculture)','ISP->0.0*NMVOC(Agriculture)','TRP->0.0*NMVOC(Agriculture)',\n"+
                    "	'BNZA->0.0*NMVOC(Agriculture)','IVOA->0.0*NMVOC(Agriculture)','TOLA->0.0*NMVOC(Agriculture)',\n"+
                    "	'PSO4(a)->0.00414306708897113*PM2.5(Agriculture)','PNO3(a)->0.00191995814110362*PM2.5(Agriculture)','PNH4(a)->0.00151575625699204*PM2.5(Agriculture)',\n"+
                    "	'PH2O(a)->0.0*PM2.5(Agriculture)','NA(a)->0.000949873942606382*PM2.5(Agriculture)','PCL(a)->0.00299614466722837*PM2.5(Agriculture)',\n"+
                    "	'PEC(a)->0.0563861329217891*PM2.5(Agriculture)','FPRM(a)->0.0247573512543725*PM2.5(Agriculture)','POA(a)->0.907331715726937*PM2.5(Agriculture)',\n"+
                    "	'PFE(a)->0.0*PM2.5(Agriculture)','PMN(a)->0.0*PM2.5(Agriculture)','PMG(a)->0.0*PM2.5(Agriculture)',\n"+
                    "	'PCA(a)->0.0*PM2.5(Agriculture)','PAL->0.0*PM2.5(Agriculture)','PK(a)->0.0*PM2.5(Agriculture)',\n"+
                    "	'PSI->0.0*PM2.5(Agriculture)','PTI->0.0*PM2.5(Agriculture)','CPRM(a)->PM10(Agriculture)'\n"+
                    "/\n";
                    out.println(CB6_r4_Agriculture);
            }catch(FileNotFoundException e){
            
                     e.printStackTrace();
            }
            
            
             System.out.println("Changes in CB6_r4_Agriculture.inp were made");
             System.out.println("Start run of anthro_emis");
            
             Runtime rt = Runtime.getRuntime();
             String[] commands = {"/bin/bash", "./anthro_emis < CB6_r4_Agriculture.inp"};
             Process proc = null;
            try {
               proc = rt.exec(commands);
            }catch (IOException e) {
               e.printStackTrace();
            }
        
        
             File file1 = new File("wrfchemi_d01");
             File file2 = new File("CAMxDIR/wrfchemi_d01_" + START_YYYY + "-" + START_MM + "-" + START_DD+ "_00:00:00_Agriculture");
             file1.renameTo(file2);
        
             File file3 = new File("wrfchemi_d02");
             File file4 = new File("CAMxDIR/wrfchemi_d02_" + START_YYYY + "-" + START_MM + "-" + START_DD+ "_00:00:00_Agriculture");
             file3.renameTo(file4);
           

            try(PrintWriter out = new PrintWriter(ANTHRODIR.getAbsolutePath()+"/CB6_r4_Energy.inp")){
            
            String CB6_r4_Energy = "&CONTROL\n"+
                    "anthro_dir = " + ANTHRODIR.getAbsolutePath() + "\n"+
                    "wrf_dir    = '/hdd4/gsakelaris/test_nc/wrf_files'\n"+
                    "src_file_prefix = 'EDGAR_HTAP_USTUTT_emi_'\n"+
                    "src_file_suffix = '_" + START_YYYY +".0.0085x0.0085_0001.nc'\n"+
                    "src_names = 'CO(28)','SO2(64)','NH3(17)','NOx(33.49456868)','NMVOC(59.384926)','PM2.5(1)','PM10(1)'\n"+
                    "sub_categories  = 'Agriculture','Energy','Industry','Residential','Transport'\n"+
                    "cat_var_prefix  = ' '\n"+
                    "serial_output   = .true.\n"+
                    "start_output_time = '" + START_YYYY +"-01-01_00:00:00'\n"+
                    "stop_output_time = '" + START_YYYY +"-01-01_00:00:00'\n"+
                    "output_interval = 3600\n"+
                    "data_yrs_offset   = 0\n"+
                    "domains = 2\n"+
                    "emissions_zdim_stag = 1\n"+
                    "emis_map = 'CO->CO(Energy)','SO2->SO2(Energy)','NH3->NH3(Energy)','CH4->0.079447322970639*NMVOC(Energy)',\n"+
                    "   'NO->0.9*NOx(Energy)','NO2->0.1*NOx(Energy)',\n"+
                    "   'ECH4->0.0*CO(Energy)','HONO->0.0*NH3(Energy)','SULF->0.0*SO2(Energy)','CL2->0.0*SO2(Energy)',\n"+
                    "   'ACET->0.0158678756476684*NMVOC(Energy)','ALD2->0.00963773747841105*NMVOC(Energy)','ALDX->0.0154360967184801*NMVOC(Energy)',\n"+
                    "	'BENZ->0.030440414507772*NMVOC(Energy)','ETH->0.0308721934369603*NMVOC(Energy)','ETHA->0.0150043177892919*NMVOC(Energy)',\n"+
                    "	'ETHY->0.0092832469775475*NMVOC(Energy)','ETOH->0.0146804835924007*NMVOC(Energy)','FORM->0.0167314335060449*NMVOC(Energy)',\n"+
                    "	'IOLE->0.0130613126079447*NMVOC(Energy)','ISOP->0.00431757340241796*NMVOC(Energy)','IVOC->0.121545768566494*NMVOC(Energy)',\n"+
                    "	'KET->0.0061987262521589*NMVOC(Energy)','MEOH->0.0161917098445596*NMVOC(Energy)','NVOL->0.00496470207253886*NMVOC(Energy)',\n"+
                    "	'OLE->0.0530008635578584*NMVOC(Energy)','PAR->0.350388601036269*NMVOC(Energy)','PRPA->0.0259067357512953*NMVOC(Energy)',\n"+
                    "	'TERP->0.00712964162348877*NMVOC(Energy)','TOL->0.053972366148532*NMVOC(Energy)','UNR->0.149287564766839*NMVOC(Energy)',\n"+
                    "	'XYL->0.0361614853195164*NMVOC(Energy)','ISP->0.0*NMVOC(Energy)','TRP->0.0*NMVOC(Energy)',\n"+
                    "	'BNZA->0.0*NMVOC(Energy)','IVOA->0.0*NMVOC(Energy)','TOLA->0.0*NMVOC(Energy)',\n"+
                    "	'PSO4(a)->0.00414306708897113*PM2.5(Energy)','PNO3(a)->0.00191995814110362*PM2.5(Energy)','PNH4(a)->0.00151575625699204*PM2.5(Energy)',\n"+
                    "	'PH2O(a)->0.0*PM2.5(Energy)','NA(a)->0.000949873942606382*PM2.5(Energy)','PCL(a)->0.00299614466722837*PM2.5(Energy)',\n"+
                    "	'PEC(a)->0.0563861329217891*PM2.5(Energy)','FPRM(a)->0.0247573512543725*PM2.5(Energy)','POA(a)->0.907331715726937*PM2.5(Energy)',\n"+
                    "	'PFE(a)->0.0*PM2.5(Energy)','PMN(a)->0.0*PM2.5(Energy)','PMG(a)->0.0*PM2.5(Energy)',\n"+
                    "	'PCA(a)->0.0*PM2.5(Energy)','PAL->0.0*PM2.5(Energy)','PK(a)->0.0*PM2.5(Energy)',\n"+
                    "	'PSI->0.0*PM2.5(Energy)','PTI->0.0*PM2.5(Energy)','CPRM(a)->PM10(Energy)'\n"+
                    "/\n";
                    out.println(CB6_r4_Energy);
            }catch(FileNotFoundException e){
            
                     e.printStackTrace();
            }
            
             System.out.println("Changes in CB6_r4_Energy.inp were made");
             System.out.println("Start run of anthro_emis");
            
             String[] commands1 = {"/bin/bash", "./anthro_emis < CB6_r4_Energy.inp"};
             Process proc1 = null;
            try {
                proc1 = rt.exec(commands1);
            }catch (IOException e){
                e.printStackTrace();
            }
           
             File file5 = new File("wrfchemi_d01");
             File file6 = new File("CAMxDIR/wrfchemi_d01_" + START_YYYY + "-" + START_MM + "-" + START_DD+ "_00:00:00_Energy");
             file5.renameTo(file6);
        
             File file7 = new File("wrfchemi_d02");
             File file8 = new File("CAMxDIR/wrfchemi_d02_" + START_YYYY + "-" + START_MM + "-" + START_DD+ "_00:00:00_Energy");
             file7.renameTo(file8);
             
             
            try(PrintWriter out = new PrintWriter(ANTHRODIR.getAbsolutePath()+"/CB6_r4_Industry.inp")){
            
            String CB6_r4_Industry = "&CONTROL\n"+
                    "anthro_dir = " + ANTHRODIR.getAbsolutePath() + "\n"+
                    "wrf_dir    = '/hdd4/gsakelaris/test_nc/wrf_files'\n"+
                    "src_file_prefix = 'EDGAR_HTAP_USTUTT_emi_'\n"+
                    "src_file_suffix = '_" + START_YYYY +".0.0085x0.0085_0001.nc'\n"+
                    "src_names = 'CO(28)','SO2(64)','NH3(17)','NOx(33.49456868)','NMVOC(59.384926)','PM2.5(1)','PM10(1)'\n"+
                    "sub_categories  = 'Agriculture','Energy','Industry','Residential','Transport'\n"+
                    "cat_var_prefix  = ' '\n"+
                    "serial_output   = .true.\n"+
                    "start_output_time = '" + START_YYYY +"-01-01_00:00:00'\n"+
                    "stop_output_time = '" + START_YYYY +"-01-01_00:00:00'\n"+
                    "output_interval = 3600\n"+
                    "data_yrs_offset   = 0\n"+
                    "domains = 2\n"+
                    "emissions_zdim_stag = 1\n"+
                    "emis_map = 'CO->CO(Industry)','SO2->SO2(Industry)','NH3->NH3(Industry)','CH4->0.079447322970639*NMVOC(Industry)',\n"+
                    "   'NO->0.9*NOx(Industry)','NO2->0.1*NOx(Industry)',\n"+
                    "   'ECH4->0.0*CO(Industry)','HONO->0.0*NH3(Industry)','SULF->0.0*SO2(Industry)','CL2->0.0*SO2(Industry)',\n"+
                    "   'ACET->0.0158678756476684*NMVOC(Industry)','ALD2->0.00963773747841105*NMVOC(Industry)','ALDX->0.0154360967184801*NMVOC(Industry)',\n"+
                    "	'BENZ->0.030440414507772*NMVOC(Industry)','ETH->0.0308721934369603*NMVOC(Industry)','ETHA->0.0150043177892919*NMVOC(Industry)',\n"+
                    "	'ETHY->0.0092832469775475*NMVOC(Industry)','ETOH->0.0146804835924007*NMVOC(Industry)','FORM->0.0167314335060449*NMVOC(Industry)',\n"+
                    "	'IOLE->0.0130613126079447*NMVOC(Industry)','ISOP->0.00431757340241796*NMVOC(Industry)','IVOC->0.121545768566494*NMVOC(Industry)',\n"+
                    "	'KET->0.0061987262521589*NMVOC(Industry)','MEOH->0.0161917098445596*NMVOC(Industry)','NVOL->0.00496470207253886*NMVOC(Industry)',\n"+
                    "	'OLE->0.0530008635578584*NMVOC(Industry)','PAR->0.350388601036269*NMVOC(Industry)','PRPA->0.0259067357512953*NMVOC(Industry)',\n"+
                    "	'TERP->0.00712964162348877*NMVOC(Industry)','TOL->0.053972366148532*NMVOC(Industry)','UNR->0.149287564766839*NMVOC(Industry)',\n"+
                    "	'XYL->0.0361614853195164*NMVOC(Industry)','ISP->0.0*NMVOC(Industry)','TRP->0.0*NMVOC(Industry)',\n"+
                    "	'BNZA->0.0*NMVOC(Industry)','IVOA->0.0*NMVOC(Industry)','TOLA->0.0*NMVOC(Industry)',\n"+
                    "	'PSO4(a)->0.00414306708897113*PM2.5(Industry)','PNO3(a)->0.00191995814110362*PM2.5(Industry)','PNH4(a)->0.00151575625699204*PM2.5(Industry)',\n"+
                    "	'PH2O(a)->0.0*PM2.5(Industry)','NA(a)->0.000949873942606382*PM2.5(Industry)','PCL(a)->0.00299614466722837*PM2.5(Industry)',\n"+
                    "	'PEC(a)->0.0563861329217891*PM2.5(Industry)','FPRM(a)->0.0247573512543725*PM2.5(Industry)','POA(a)->0.907331715726937*PM2.5(Industry)',\n"+
                    "	'PFE(a)->0.0*PM2.5(Industry)','PMN(a)->0.0*PM2.5(Industry)','PMG(a)->0.0*PM2.5(Industry)',\n"+
                    "	'PCA(a)->0.0*PM2.5(Industry)','PAL->0.0*PM2.5(Industry)','PK(a)->0.0*PM2.5(Industry)',\n"+
                    "	'PSI->0.0*PM2.5(Industry)','PTI->0.0*PM2.5(Industry)','CPRM(a)->PM10(Industry)'\n"+
                    "/\n";
                    out.println(CB6_r4_Industry);
            }catch(FileNotFoundException e){
            
                     e.printStackTrace();
            }
            
            System.out.println("Changes in CB6_r4_Industry.inp were made");
            System.out.println("Start run of anthro_emis");
            
             String[] commands2 = {"/bin/bash", "./anthro_emis < CB6_r4_Industry.inp"};
             Process proc2 = null;
            try {
                proc2 = rt.exec(commands2);
            }catch (IOException e){
                e.printStackTrace();
            }
            
             File file9 = new File("wrfchemi_d01");
             File file10 = new File("CAMxDIR/wrfchemi_d01_" + START_YYYY + "-" + START_MM + "-" + START_DD+ "_00:00:00_Industry");
             file9.renameTo(file10);
        
             File file11 = new File("wrfchemi_d02");
             File file12 = new File("CAMxDIR/wrfchemi_d02_" + START_YYYY + "-" + START_MM + "-" + START_DD+ "_00:00:00_Industry");
             file11.renameTo(file12);
             
            try(PrintWriter out = new PrintWriter(ANTHRODIR.getAbsolutePath()+"/CB6_r4_Transport.inp")){
            
            String CB6_r4_Transport = "&CONTROL\n"+
                    "anthro_dir = " + ANTHRODIR.getAbsolutePath() + "\n"+
                    "wrf_dir    = '/hdd4/gsakelaris/test_nc/wrf_files'\n"+
                    "src_file_prefix = 'EDGAR_HTAP_USTUTT_emi_'\n"+
                    "src_file_suffix = '_" + START_YYYY +".0.0085x0.0085_0001.nc'\n"+
                    "src_names = 'CO(28)','SO2(64)','NH3(17)','NOx(33.49456868)','NMVOC(59.384926)','PM2.5(1)','PM10(1)'\n"+
                    "sub_categories  = 'Agriculture','Energy','Industry','Residential','Transport'\n"+
                    "cat_var_prefix  = ' '\n"+
                    "serial_output   = .true.\n"+
                    "start_output_time = '" + START_YYYY +"-01-01_00:00:00'\n"+
                    "stop_output_time = '" + START_YYYY +"-01-01_00:00:00'\n"+
                    "output_interval = 3600\n"+
                    "data_yrs_offset   = 0\n"+
                    "domains = 2\n"+
                    "emissions_zdim_stag = 1\n"+
                    "emis_map = 'CO->CO(Transport)','SO2->SO2(Transport)','NH3->NH3(Transport)','CH4->0.079447322970639*NMVOC(Transport)',\n"+
                    "   'NO->0.9*NOx(Transport)','NO2->0.1*NOx(Transport)',\n"+
                    "   'ECH4->0.0*CO(Transport)','HONO->0.0*NH3(Transport)','SULF->0.0*SO2(Transport)','CL2->0.0*SO2(Transport)',\n"+
                    "   'ACET->0.0158678756476684*NMVOC(Transport)','ALD2->0.00963773747841105*NMVOC(Transport)','ALDX->0.0154360967184801*NMVOC(Transport)',\n"+
                    "	'BENZ->0.030440414507772*NMVOC(Transport)','ETH->0.0308721934369603*NMVOC(Transport)','ETHA->0.0150043177892919*NMVOC(Transport)',\n"+
                    "	'ETHY->0.0092832469775475*NMVOC(Transport)','ETOH->0.0146804835924007*NMVOC(Transport)','FORM->0.0167314335060449*NMVOC(Transport)',\n"+
                    "	'IOLE->0.0130613126079447*NMVOC(Transport)','ISOP->0.00431757340241796*NMVOC(Transport)','IVOC->0.121545768566494*NMVOC(Transport)',\n"+
                    "	'KET->0.0061987262521589*NMVOC(Transport)','MEOH->0.0161917098445596*NMVOC(Transport)','NVOL->0.00496470207253886*NMVOC(Transport)',\n"+
                    "	'OLE->0.0530008635578584*NMVOC(Transport)','PAR->0.350388601036269*NMVOC(Transport)','PRPA->0.0259067357512953*NMVOC(Transport)',\n"+
                    "	'TERP->0.00712964162348877*NMVOC(Transport)','TOL->0.053972366148532*NMVOC(Transport)','UNR->0.149287564766839*NMVOC(Transport)',\n"+
                    "	'XYL->0.0361614853195164*NMVOC(Transport)','ISP->0.0*NMVOC(Transport)','TRP->0.0*NMVOC(Transport)',\n"+
                    "	'BNZA->0.0*NMVOC(Transport)','IVOA->0.0*NMVOC(Transport)','TOLA->0.0*NMVOC(Transport)',\n"+
                    "	'PSO4(a)->0.00414306708897113*PM2.5(Transport)','PNO3(a)->0.00191995814110362*PM2.5(Transport)','PNH4(a)->0.00151575625699204*PM2.5(Transport)',\n"+
                    "	'PH2O(a)->0.0*PM2.5(Transport)','NA(a)->0.000949873942606382*PM2.5(Transport)','PCL(a)->0.00299614466722837*PM2.5(Transport)',\n"+
                    "	'PEC(a)->0.0563861329217891*PM2.5(Transport)','FPRM(a)->0.0247573512543725*PM2.5(Transport)','POA(a)->0.907331715726937*PM2.5(Transport)',\n"+
                    "	'PFE(a)->0.0*PM2.5(Transport)','PMN(a)->0.0*PM2.5(Transport)','PMG(a)->0.0*PM2.5(Transport)',\n"+
                    "	'PCA(a)->0.0*PM2.5(Transport)','PAL->0.0*PM2.5(Transport)','PK(a)->0.0*PM2.5(Transport)',\n"+
                    "	'PSI->0.0*PM2.5(Transport)','PTI->0.0*PM2.5(Transport)','CPRM(a)->PM10(Transport)'\n"+
                    "/\n";
                    out.println(CB6_r4_Transport);
            }catch(FileNotFoundException e){
            
                     e.printStackTrace();
            } 
            
            System.out.println("Changes in CB6_r4_Transport.inp were made");
            System.out.println("Start run of anthro_emis");
            
             String[] commands3 = {"/bin/bash", "./anthro_emis < CB6_r4_Transport.inp"};
             Process proc3 = null;
            try {
                proc3 = rt.exec(commands3);
            }catch (IOException e){
                e.printStackTrace();
            }
            
             File file13 = new File("wrfchemi_d01");
             File file14 = new File("CAMxDIR/wrfchemi_d01_" + START_YYYY + "-" + START_MM + "-" + START_DD+ "_00:00:00_Transport");
             file13.renameTo(file14);
        
             File file15 = new File("wrfchemi_d02");
             File file16 = new File("CAMxDIR/wrfchemi_d02_" + START_YYYY + "-" + START_MM + "-" + START_DD+ "_00:00:00_Transport");
             file15.renameTo(file16);
             
             
            try(PrintWriter out = new PrintWriter(ANTHRODIR.getAbsolutePath()+"/CB6_r4_Residential.inp")){
            
            String CB6_r4_Residential = "&CONTROL\n"+
                    "anthro_dir = " + ANTHRODIR.getAbsolutePath() + "\n"+
                    "wrf_dir    = '/hdd4/gsakelaris/test_nc/wrf_files'\n"+
                    "src_file_prefix = 'EDGAR_HTAP_USTUTT_emi_'\n"+
                    "src_file_suffix = '_" + START_YYYY +".0.0085x0.0085_0001.nc'\n"+
                    "src_names = 'CO(28)','SO2(64)','NH3(17)','NOx(33.49456868)','NMVOC(59.384926)','PM2.5(1)','PM10(1)'\n"+
                    "sub_categories  = 'Agriculture','Energy','Industry','Residential','Transport'\n"+
                    "cat_var_prefix  = ' '\n"+
                    "serial_output   = .true.\n"+
                    "start_output_time = '" + START_YYYY +"-01-01_00:00:00'\n"+
                    "stop_output_time = '" + START_YYYY +"-01-01_00:00:00'\n"+
                    "output_interval = 3600\n"+
                    "data_yrs_offset   = 0\n"+
                    "domains = 2\n"+
                    "emissions_zdim_stag = 1\n"+
                    "emis_map = 'CO->CO(Residential)','SO2->SO2(Residential)','NH3->NH3(Residential)','CH4->0.079447322970639*NMVOC(Residential)',\n"+
                    "   'NO->0.9*NOx(Residential)','NO2->0.1*NOx(Residential)',\n"+
                    "   'ECH4->0.0*CO(Residential)','HONO->0.0*NH3(Residential)','SULF->0.0*SO2(Residential)','CL2->0.0*SO2(Residential)',\n"+
                    "   'ACET->0.0158678756476684*NMVOC(Residential)','ALD2->0.00963773747841105*NMVOC(Residential)','ALDX->0.0154360967184801*NMVOC(Residential)',\n"+
                    "	'BENZ->0.030440414507772*NMVOC(Residential)','ETH->0.0308721934369603*NMVOC(Residential)','ETHA->0.0150043177892919*NMVOC(Residential)',\n"+
                    "	'ETHY->0.0092832469775475*NMVOC(Residential)','ETOH->0.0146804835924007*NMVOC(Residential)','FORM->0.0167314335060449*NMVOC(Residential)',\n"+
                    "	'IOLE->0.0130613126079447*NMVOC(Residential)','ISOP->0.00431757340241796*NMVOC(Residential)','IVOC->0.121545768566494*NMVOC(Residential)',\n"+
                    "	'KET->0.0061987262521589*NMVOC(Residential)','MEOH->0.0161917098445596*NMVOC(Residential)','NVOL->0.00496470207253886*NMVOC(Residential)',\n"+
                    "	'OLE->0.0530008635578584*NMVOC(Residential)','PAR->0.350388601036269*NMVOC(Residential)','PRPA->0.0259067357512953*NMVOC(Residential)',\n"+
                    "	'TERP->0.00712964162348877*NMVOC(Residential)','TOL->0.053972366148532*NMVOC(Residential)','UNR->0.149287564766839*NMVOC(Residential)',\n"+
                    "	'XYL->0.0361614853195164*NMVOC(Residential)','ISP->0.0*NMVOC(Residential)','TRP->0.0*NMVOC(Residential)',\n"+
                    "	'BNZA->0.0*NMVOC(Residential)','IVOA->0.0*NMVOC(Residential)','TOLA->0.0*NMVOC(Residential)',\n"+
                    "	'PSO4(a)->0.00414306708897113*PM2.5(Residential)','PNO3(a)->0.00191995814110362*PM2.5(Residential)','PNH4(a)->0.00151575625699204*PM2.5(Residential)',\n"+
                    "	'PH2O(a)->0.0*PM2.5(Residential)','NA(a)->0.000949873942606382*PM2.5(Residential)','PCL(a)->0.00299614466722837*PM2.5(Residential)',\n"+
                    "	'PEC(a)->0.0563861329217891*PM2.5(Residential)','FPRM(a)->0.0247573512543725*PM2.5(Residential)','POA(a)->0.907331715726937*PM2.5(Residential)',\n"+
                    "	'PFE(a)->0.0*PM2.5(Residential)','PMN(a)->0.0*PM2.5(Residential)','PMG(a)->0.0*PM2.5(Residential)',\n"+
                    "	'PCA(a)->0.0*PM2.5(Residential)','PAL->0.0*PM2.5(Residential)','PK(a)->0.0*PM2.5(Residential)',\n"+
                    "	'PSI->0.0*PM2.5(Residential)','PTI->0.0*PM2.5(Residential)','CPRM(a)->PM10(Residential)'\n"+
                    "/\n";
                    out.println(CB6_r4_Residential);
            }catch(FileNotFoundException e){
            
                     e.printStackTrace();
            }
            
             System.out.println("Changes in CB6_r4_Residential.inp were made");
             System.out.println("Start run of anthro_emis");
             
             String[] commands4 = {"/bin/bash", "./anthro_emis < CB6_r4_Residential.inp"};
             Process proc4 = null;
            try {
                proc4 = rt.exec(commands4);
            }catch (IOException e){
                e.printStackTrace();
            }
            
             //cp ../wrfchemi_d01* .
             //cp ../wrfchemi_d02* .
             
             if(CAMxDIR.isFile()){
                
                File dest = new File(CAMxDIR.getAbsolutePath());
                File src = new File("../wrfchemi_d01");
                try{
                EmissInvCopy.copyFile(src,dest);
                }catch (IOException e) {
                    e.printStackTrace();
                }
              }
             
             if(CAMxDIR.isFile()){
                
                File dest1 = new File(CAMxDIR.getAbsolutePath());
                File src1 = new File("../wrfchemi_d02");
                try{
                EmissInvCopy.copyFile(src1,dest1);
                }catch (IOException e) {
                    e.printStackTrace();
                }
              }
             
             
             
             File file17 = new File("wrfchemi_d01");
             File file18 = new File("CAMxDIR/wrfchemi_d01_" + START_YYYY + "-" + START_MM + "-" + START_DD+ "_00:00:00_Residential");
             file17.renameTo(file18);
        
             File file19 = new File("wrfchemi_d02");
             File file20 = new File("CAMxDIR/wrfchemi_d02_" + START_YYYY + "-" + START_MM + "-" + START_DD+ "_00:00:00_Residential");
             file19.renameTo(file20);
             
             //Anthro emiss procedure finished! All wrfchemi files are ready.
             //Temporal profiles procedure follows.
             //Copy wrfchemi files to new dir for safety. Return to CAMxDIR and update scenario there
             
             File wrfchemi_ini = new File("wrfchemi_ini");
             if(wrfchemi_ini.isDirectory()){
                 wrfchemi_ini.mkdir();
             }
             
             if(wrfchemi_ini.isFile()){
                 File destDir1 = new File(wrfchemi_ini.getAbsolutePath());
                 File srcFile1 = new File("../wrfchemi_d*");
                 try{
                 EmissInvCopy.copyFile(srcFile1, destDir1);
                 }catch (IOException e) {
                     e.printStackTrace();
                 }
               }
             //cd wrfchemi_ini
             //rm *
             //cp ../wrfchemi_d* .
             
             
             if(CAMxDIR.isFile()){
                 File destDir2 = new File(CAMxDIR.getAbsolutePath());
                 File srcFile2 = new File("../wrfchemi_d*");
                 try{
                 EmissInvCopy.copyFile(srcFile2, destDir2);
                 }catch (IOException e) {
                     e.printStackTrace();
                 }
               }
             //cd $CAMxDIR
             //cp ../Coef_calc_wrfchemi_cb6.py .
             
             
             String[] cmnds = {"python", "Coef_calc_wrfchemi_cb6.py" +START_YYYY, "" +START_MM, "" +START_DD, "d01"};
             Process procc = null;
             try {
             procc = rt.exec(cmnds);
             } catch (IOException e) {
                 e.printStackTrace();
             }
            
            
             String[] cmnds1 = {"python", "Coef_calc_wrfchemi_cb6.py" +START_YYYY, "" +START_MM, "" +START_DD, "" +RUN_GRID};
             Process procc1 = null;
             try {
             procc1 = rt.exec(cmnds1);
             } catch (IOException e) {
                 e.printStackTrace();
             }
             //python Coef_calc_wrfchemi_cb6.py $START_YYYY $START_MM $START_DD d01
             //python Coef_calc_wrfchemi_cb6.py $START_YYYY $START_MM $START_DD $RUN_GRID
             }
        else{
            System.out.println("AQM can be 0 or 1. Choose correctly!");
        }
    }
}
