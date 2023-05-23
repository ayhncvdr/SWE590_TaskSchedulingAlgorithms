package org.cloudbus.cloudsim.examples;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.RoundRobinDatacenterBroker;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

public class RoundRobinTaskScheduling {
	/** The cloudlet list. */
	private static List<Cloudlet> cloudletList;

	/** The vmlist. */

	private static List<Vm> createVM(int vmId,int brokerId, int vms, int mipValue) {
	    List<Vm> vmList = new ArrayList<Vm>();

	    // VM Parameters
	    long size = 10000; // image size (MB)
	    int ram = 512; // vm memory (MB)
	    int mips = mipValue;
	    long bw = 1000;
	    int pesNumber = 1; // number of CPUs
	    String vmm = "Xen"; // VMM name

	    // Create VMs
	    for (int i = 0; i < vms; i++) {
	        Vm vm = new Vm(vmId, brokerId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerSpaceShared());
	        vmList.add(vm);
	    }

	    return vmList;
	}


	private static List<Cloudlet> createCloudlet(int userId, int cloudlets) {
	    // Creates a container to store Cloudlets
	    LinkedList<Cloudlet> list = new LinkedList<Cloudlet>();

	    // Cloudlet parameters
	    long baseLength = 5000;
	    long fileSize = 300;
	    long outputSize = 300;
	    int pesNumber = 1;
	    UtilizationModel utilizationModel = new UtilizationModelFull();

	    Cloudlet[] cloudlet = new Cloudlet[cloudlets];

	    for (int i = 0; i < cloudlets; i++) {
	        long length = Math.max(baseLength - (i * 100), 1000); // Adjust the length for each cloudlet, ensuring a minimum of 1000
	        cloudlet[i] = new Cloudlet(i, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
	        // Setting the owner of these Cloudlets
	        cloudlet[i].setUserId(userId);
	        if (i % 2 == 0) {
	            list.addLast(cloudlet[i]); // If i is even, add the cloudlet to the end of the list
	        } else {
	            list.addFirst(cloudlet[i]); // If i is odd, add the cloudlet to the beginning of the list
	        }
	    }

	    System.out.println("TEST");
	    for (Cloudlet c : list) {
	        System.out.println("Cloudlet Id: " + c.getCloudletId() + ", Cloudlet Length: " + c.getCloudletLength());
	    }
	    System.out.println("TEST");

	    return list;
	}





	////////////////////////// STATIC METHODS ///////////////////////

	/**
	 * Creates main() to run this example
	 */
	public static void main(String[] args) {
		Log.printLine("========= RR Task Scheduling Algorithm Implementation ========");

		try {
			Log.printLine("======== Starting Execution ========");
			// First step: Initialize the CloudSim package. It should be called
			// before creating any entities.
			int num_user = 3;   // number of grid users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false;  // mean trace events

			// Initialize the CloudSim library
			CloudSim.init(num_user, calendar, trace_flag);

			// Second step: Create Datacenters
			//Datacenters are the resource providers in CloudSim. We need at list one of them to run a CloudSim simulation

			@SuppressWarnings("unused")
			Datacenter datacenter0 = createDatacenter("Datacenter_0");

			//Third step: Create Broker
			RoundRobinDatacenterBroker broker = createBroker();
			int brokerId = broker.getId();

			//Fourth step: Create VMs and Cloudlets and send them to broker
			List<Vm> vmList = new ArrayList<Vm>();

			vmList.addAll(createVM(1,brokerId, 1, 300));
			vmList.addAll(createVM(2,brokerId, 1, 500));

			cloudletList = createCloudlet(brokerId,30); // creating 40 cloudlets

			broker.submitVmList(vmList);
			broker.submitCloudletList(cloudletList);

			// Fifth step: Starts the simulation
			CloudSim.startSimulation();

			// Final step: Print results when simulation is over
			List<Cloudlet> newList = broker.getCloudletReceivedList();

			CloudSim.stopSimulation();

			printCloudletList(newList);
			Log.printLine("RR has finished executing!");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Log.printLine("The simulation has been terminated due to an unexpected error");
		}
	}

	private static Datacenter createDatacenter(String name){

		// Here are the steps needed to create a PowerDatacenter:
		// 1. We need to create a list to store one or more
		//    Machines
		List<Host> hostList = new ArrayList<Host>();

		// 2. A Machine contains one or more PEs or CPUs/Cores. Therefore, should
		//    create a list to store these PEs before creating
		//    a Machine.
		List<Pe> peList1 = new ArrayList<Pe>();

		int mips = 1000;

		// 3. Create PEs and add these into the list.
		//for a quad-core machine, a list of 4 PEs is required:
		peList1.add(new Pe(0, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating
		peList1.add(new Pe(1, new PeProvisionerSimple(mips)));
		peList1.add(new Pe(2, new PeProvisionerSimple(mips)));
		peList1.add(new Pe(3, new PeProvisionerSimple(mips)));

		//Another list, for a dual-core machine
		List<Pe> peList2 = new ArrayList<Pe>();

		peList2.add(new Pe(0, new PeProvisionerSimple(mips)));
		peList2.add(new Pe(1, new PeProvisionerSimple(mips)));

		//4. Create Hosts with its id and list of PEs and add them to the list of machines
		int hostId=0;
		int ram = 2048; //host memory (MB)
		long storage = 1000000; //host storage
		int bw = 10000;

		hostList.add(
    			new Host(
    				hostId,
    				new RamProvisionerSimple(ram),
    				new BwProvisionerSimple(bw),
    				storage,
    				peList1,
    				new VmSchedulerTimeShared(peList1)
    			)
    		); // This is our first machine


		// 5. Create a DatacenterCharacteristics object that stores the
		//    properties of a data center: architecture, OS, list of
		//    Machines, allocation policy: time- or space-shared, time zone
		//    and its price (G$/Pe time unit).
		String arch = "x86";      // system architecture
		String os = "Linux";          // operating system
		String vmm = "Xen";
		double time_zone = 10.0;         // time zone this resource located
		double cost = 3.0;              // the cost of using processing in this resource
		double costPerMem = 0.05;		// the cost of using memory in this resource
		double costPerStorage = 0.1;	// the cost of using storage in this resource
		double costPerBw = 0.1;			// the cost of using bw in this resource
		LinkedList<Storage> storageList = new LinkedList<Storage>();	//we are not adding SAN devices by now

		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);


		// 6. Finally, we need to create a PowerDatacenter object.
		Datacenter datacenter = null;
		try {
			datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return datacenter;
	}
	//We strongly encourage users to develop their own broker policies, to submit vms and cloudlets according
	//to the specific rules of the simulated scenario
	private static RoundRobinDatacenterBroker createBroker(){

		
		RoundRobinDatacenterBroker broker = null;
		try {
			broker = new RoundRobinDatacenterBroker("Broker");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return broker;
	}

	/**
	 * Prints the Cloudlet objects
	 * @param list  list of Cloudlets
	 */
	private static void printCloudletList(List<Cloudlet> list) {
	    int size = list.size();
	    Cloudlet cloudlet;

	    double totalCpuUtilization = 0.0;
	    double avgCpuUtilization;
	    String indent = "    ";
	    Log.printLine();
	    Log.printLine("========== OUTPUT ==========");
	    Log.printLine("Cloudlet ID" + indent + "STATUS" + indent +
	            "Data center ID" + indent + "VM ID" + indent + indent + "Time" + indent + "Start Time" + indent + "Finish Time" + indent + "user id" + indent + "Turnaround Time" + indent + "CPU Utilization (%)");

	    DecimalFormat dft = new DecimalFormat("###.##");
	    double totalTurnaroundTime = 0.0;
	    double totalResponseTime = 0.0;
	    double makespan = 0.0; // Initialize makespan to 0
	    int numCompletedCloudlets = 0;
	    for (int i = 0; i < size; i++) {
	        cloudlet = list.get(i);
	        Log.print(indent + cloudlet.getCloudletId() + indent + indent);

	        if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
	            Log.print("SUCCESS");

	            double turnaroundTime = cloudlet.getFinishTime() - cloudlet.getSubmissionTime();
	            double cpuUtilization = (cloudlet.getActualCPUTime() / turnaroundTime) * 100;
	            double responseTime = cloudlet.getExecStartTime() - cloudlet.getSubmissionTime();

	            totalTurnaroundTime += turnaroundTime;
	            totalCpuUtilization += cpuUtilization;
	            totalResponseTime += responseTime;

	            Log.printLine(indent + indent + cloudlet.getResourceId() + indent + indent + indent + cloudlet.getVmId() +
	                    indent + indent + indent + dft.format(cloudlet.getActualCPUTime()) +
	                    indent + indent + dft.format(cloudlet.getExecStartTime()) + indent + indent + indent + dft.format(cloudlet.getFinishTime()) + indent + indent + indent + cloudlet.getUserId() + indent + indent + indent + dft.format(turnaroundTime) + indent + indent + indent + dft.format(cpuUtilization) + indent + indent + indent + dft.format(responseTime));

	            makespan = Math.max(makespan, cloudlet.getFinishTime()); // Update makespan if necessary
	            numCompletedCloudlets++;
	        }
	    }
	    Log.printLine("Makespan: " + dft.format(makespan));
	    double avgTurnaroundTime = totalTurnaroundTime / numCompletedCloudlets;
	    Log.printLine("Average Turnaround Time: " + dft.format(avgTurnaroundTime));
	    avgCpuUtilization = totalCpuUtilization / size;
	    Log.printLine("Average CPU Utilization: " + dft.format(avgCpuUtilization) + "%");
	    double avgResponseTime = totalResponseTime / numCompletedCloudlets;
	    Log.printLine("Average Response Time: " + dft.format(avgResponseTime));

	}
}