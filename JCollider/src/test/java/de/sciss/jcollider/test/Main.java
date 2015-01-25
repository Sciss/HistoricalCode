package de.sciss.jcollider.test;

import de.sciss.jcollider.JCollider;
import de.sciss.jcollider.UGenInfo;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

public class Main {
    public static void main( String args[] )
    {
        final String demoClass;

        if( args.length == 1 ) {
            final String arg1 = args[ 0 ];
            if( arg1.equals( "--test1" )) {
                demoClass = "de.sciss.jcollider.test.Demo";
            } else if( arg1.equals( "--test2" )) {
                demoClass = "de.sciss.jcollider.test.MotoRevCtrl";
            } else if( arg1.equals( "--test3" )) {
                demoClass = "de.sciss.jcollider.test.BusTests";
            } else if( arg1.equals( "--bindefs" )) {
                try {
                    UGenInfo.readDefinitions();
                    UGenInfo.writeBinaryDefinitions( new File("de/sciss/jcollider/ugendefs.bin"));
                }
                catch( IOException e1 ) {
                    e1.printStackTrace();
                }
                demoClass = null;
                System.exit( 0 );
            } else {
                demoClass = null;
            }
        } else {
            demoClass = null;
        }

        if( demoClass != null ) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    try {
                        final Class c = Class.forName(demoClass);
                        c.newInstance();
                    } catch (Exception e1) {
                        System.err.println(e1);
                        System.exit(1);
                    }
                }
            });

        } else {
            System.err.println( "\nJCollider v" + JCollider.VERSION + "\n" +
                    JCollider.getCopyrightString() + "\n\n" +
                    JCollider.getCreditsString() + "\n\n  " +
                    JCollider.getResourceString("errIsALibrary"));

            System.out.println( "\nThe following options are available:\n"+
                    "--test1    SynthDef demo\n"+
                    "--test2    MotoRev Control Demo\n"+
                    "--test3    Bus Tests\n"+
                    "--bindefs  Create Binary UGen Definitions\n");
            System.exit( 1 );
        }
    }
}
