package com.example.demo.metier;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dao.CompteRepository;
import com.example.demo.dao.OperationRepository;
import com.example.demo.entities.Compte;
import com.example.demo.entities.CompteCourant;
import com.example.demo.entities.Operation;
import com.example.demo.entities.Retrait;
import com.example.demo.entities.Versement;
@Service
@Transactional
public class BanqueMetierImpl implements IBanqueMetier {
	@Autowired
	private CompteRepository compteRepository;
	@Autowired
	private OperationRepository operationRepository;
	
	@Override
	public Compte consulterCompte(String codeCpte) {
		Compte cp=compteRepository.findOne(codeCpte);
		if(cp==null) throw new RuntimeException("Compte intouvable");
		return cp;
	}

	@Override
	public void verser(String codeCpte, double montant) {
		Compte cp=consulterCompte(codeCpte);
		Versement v=new Versement(new Date(), montant, cp);
		operationRepository.save(v);
		cp.setSolde(cp.getSolde()+montant);
		compteRepository.save(cp);
		
	}

	@Override
	public void retirer(String codeCpte, double montant) {
		
		Compte cp=consulterCompte(codeCpte);
		double facilitesCaisse=0;
		if (cp instanceof CompteCourant )
			facilitesCaisse=((CompteCourant) cp).getDecouvert();
		if (cp.getSolde()+facilitesCaisse<montant)
			throw new RuntimeException("Solde insuffisant");
		Retrait r=new Retrait(new Date(), montant, cp);
		operationRepository.save(r);
		cp.setSolde(cp.getSolde()- montant);
		compteRepository.save(cp);
		
		
	}

	@Override
	public void virement(String codeCpte1, String codeCpte2, double montant) {
		if( codeCpte1.equals(codeCpte2) )
			throw new RuntimeException("Impossible d'effectuer un virement sur le meme compte");
			
		retirer(codeCpte1, montant);
		verser(codeCpte2, montant);
		
		
	}

	@Override
	public Page<Operation> listOperation(String codeCpte, int page, int size) {
		
		return operationRepository.listOperation(codeCpte, new PageRequest(page, size));
	}
	

}
