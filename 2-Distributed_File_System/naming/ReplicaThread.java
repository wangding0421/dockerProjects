package naming;

import java.io.*;
import rmi.*;

/******************************************************************************
 *
 * Authors: Christopher Tomaszewski (CKT) & Dinesh Palanisamy (DINESHP)
 *
 ******************************************************************************/

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import storage.Command;
import storage.Storage;
import common.Path;

public class ReplicaThread implements Runnable {

	private Path path;
	private Command replicaCommand;
	private Set<Storage> existedStorages;
	private Storage replicaMachine;

	/* Initializes objects needed to replicate and update data structures */
	public ReplicaThread(Path path, Command replicaCommand, Set<Storage> existedStorages,
                                        Storage replicaMachine) {
		this.path = path;
		this.replicaCommand = replicaCommand;
		this.existedStorages = existedStorages;
		this.replicaMachine = replicaMachine;
	}

	public void run() {

		try{
			replicaCommand.copy(path, existedStorages.iterator().next());
		} catch (RMIException e) {
            System.out.println(e.getCause());
        } catch (FileNotFoundException e) {
            System.out.println(e.getCause());
        } catch (IOException e) {
            System.out.println(e.getCause());
        }

		existedStorages.add(replicaMachine);
	}


}
