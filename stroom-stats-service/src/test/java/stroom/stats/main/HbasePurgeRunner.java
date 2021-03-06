

/*
 * Copyright 2017 Crown Copyright
 *
 * This file is part of Stroom-Stats.
 *
 * Stroom-Stats is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Stroom-Stats is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Stroom-Stats.  If not, see <http://www.gnu.org/licenses/>.
 */

package stroom.stats.main;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import stroom.stats.HBaseClient;

import java.util.Scanner;

//needs to extend ThreadScopeRunnable as the AOP marshaling of the StatisticDataSource (specifically the  MarshalOptions bean) needs thread scope
public class HbasePurgeRunner {
    public static void main(final String[] args) throws Exception {
        boolean proceed = false;

        Scanner consoleInScanner = null;

        try {
            consoleInScanner = new Scanner(System.in);

            System.out.println("This process will run the purge process, thus deleting lots of data!\n\n");

            System.out.println("Do you wish to continue? [yes|no]\n");

            while (consoleInScanner.hasNext()) {
                if (consoleInScanner.next().equals("yes")) {
                    proceed = true;

                }
                break;
            }
        } finally {
            if (consoleInScanner != null) {
                consoleInScanner.close();
            }

        }

        if (proceed) {
            final Injector injector = Guice.createInjector(new AbstractModule() {
                @Override
                protected void configure() {
                    bind(HBaseClient.class);
                }
            });

            final HBaseClient hBaseClient = injector.getInstance(HBaseClient.class);
            hBaseClient.purgeAllData();
            System.out.println("\nAll done!");

        } else {
            System.out.println("Exiting");
            System.exit(0);
        }
    }

}
