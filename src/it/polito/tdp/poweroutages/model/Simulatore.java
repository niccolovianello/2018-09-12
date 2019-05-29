package it.polito.tdp.poweroutages.model;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;

import it.polito.tdp.poweroutages.model.Evento.TIPO;

public class Simulatore {
	
	// Modello/Stato del sistema
	private Graph<Nerc, DefaultWeightedEdge> grafo;
	private List<PowerOutage> powerOutages;
	private Map<Nerc, Set<Nerc>> prestiti;
	
	// Parametri della simulazione
	private int k;
	
	// Valori in output
	private int CATASTROFI;
	private Map<Nerc, Long> bonus;
	
	// Coda
	private  PriorityQueue<Evento> queue;
	
	public void init(int k, List<PowerOutage> powerOutages, NercIdMap nerc, Graph<Nerc, DefaultWeightedEdge> grafo) {
		
		this.queue = new PriorityQueue<>();
		this.bonus = new HashMap<>();
		this.prestiti = new HashMap<>();
		
		for(Nerc n : nerc.values()) {
			this.bonus.put(n, Long.valueOf(0));
			this.prestiti.put(n, new HashSet<Nerc>());
		}
		
		this.CATASTROFI = 0;
		
		this.k = k;
		this.powerOutages = powerOutages;
		this.grafo = grafo;
		
		// inserisco gli eventi iniziali
		
		for(PowerOutage po : this.powerOutages) {
			Evento e = new Evento(Evento.TIPO.INIZIO_INTERRUZIONE, po.getNerc(), null, po.getInizio(), po.getInizio(), po.getFine());
			queue.add(e);
		}
	}
	
	public void run() {
		
		Evento e;
		
		while((e = queue.poll()) != null) {
			
			switch(e.getTipo()) {
			case INIZIO_INTERRUZIONE:
				
				Nerc nerc = e.getNerc();
				
				// cerco se c'è un donatore altrimenti CATASTROFE
				Nerc donatore = null;
				
				// cerco tra i miei debitori
				if(this.prestiti.get(nerc).size()>0) {
					// scelgo tra i miei debitori
					double min = Long.MAX_VALUE;
					for(Nerc n : this.prestiti.get(nerc)) {
						DefaultWeightedEdge edge = this.grafo.getEdge(nerc, n);
						if(this.grafo.getEdgeWeight(edge) < min) {
							if(!n.getStaPrestando()) {
								donatore = n;
								min = this.grafo.getEdgeWeight(edge);
							}
							
						}
					}
					
				} else {
					// prendo quello con il peso dell'arco minore
					double min = Long.MAX_VALUE;
					List<Nerc> neighbors = Graphs.neighborListOf(this.grafo, nerc);
					for(Nerc n : neighbors) {
						DefaultWeightedEdge edge = this.grafo.getEdge(nerc, n);
						if(this.grafo.getEdgeWeight(edge) < min) {
							if(!n.getStaPrestando()) {
								donatore = n;
								min = this.grafo.getEdgeWeight(edge);
							}
							
						}
					}
				}
				
				if(donatore != null) {
					// trovato
					System.out.println("TROVATO DONATORE: "+donatore);
					donatore.setStaPrestando(true);
					Evento fine = new Evento(Evento.TIPO.FINE_INTERRUZIONE, e.getNerc(), donatore, e.getDataFine(), e.getDataInizio(),  e.getDataFine());
					queue.add(fine);
					this.prestiti.get(donatore).add(e.getNerc());
					Evento cancella = new Evento(Evento.TIPO.CANCELLA_PRESTITO, e.getNerc(), donatore, e.getData().plusMonths(k), e.getDataInizio(),  e.getDataFine());
					queue.add(cancella);
				}
				
				else {
					// CATASTROFE
					this.CATASTROFI++;
					System.out.println("CATASTROFE IN CORSO");
				}
				
				
				break;
			case FINE_INTERRUZIONE:
				System.out.println("FINE INTERRUZIONE NERC: "+e.getNerc());
				// assegnare un bonus al donatore
				if(e.getDonatore() != null) {
					this.bonus.put(e.getDonatore(), bonus.get(e.getDonatore()) + Duration.between(e.getDataInizio(), e.getDataFine()).toDays());
				}
				
				// dire che il donatore non sta più prestando
				e.getDonatore().setStaPrestando(false);
				
				break;
			case CANCELLA_PRESTITO:
				System.out.println("CANCELLAZIONE PRESTITO "+e.getNerc());
				this.prestiti.remove(e.getDonatore()).remove(e.getNerc());
				break;
			}
		}
		
	}

}
