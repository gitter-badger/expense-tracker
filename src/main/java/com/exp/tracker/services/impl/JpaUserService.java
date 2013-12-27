package com.exp.tracker.services.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.webflow.context.ExternalContext;

import com.exp.tracker.data.entities.AuthEntity;
import com.exp.tracker.data.entities.RoleEntity;
import com.exp.tracker.data.entities.UserEntity;
import com.exp.tracker.data.model.AuthBean;
import com.exp.tracker.data.model.PasswordChangeBean;
import com.exp.tracker.data.model.UserBean;
import com.exp.tracker.services.api.UserService;
import com.exp.tracker.utils.IPasswordEncoder;

@Service("userService")
@Repository
public class JpaUserService implements UserService {

	private EntityManager em;
	private IPasswordEncoder passwordEncoder;

	@PersistenceContext
	public void setEntityManager(EntityManager em) {
		this.em = em;
	}

	@Transactional
	public UserBean addUser(UserBean ub) {
		UserBean newUb = null;
		boolean userExists = true;
		// first find match
		Query queryFindMatch = em.createNamedQuery("findUserMatch");
		queryFindMatch.setParameter("username", ub.getUsername());
		//queryFindMatch.setParameter("emailId", ub.getEmailId());
		@SuppressWarnings("unused")
		UserEntity ue = null;
		try {
			ue = (UserEntity) queryFindMatch.getSingleResult();
		} catch (NoResultException nre) {
			userExists = false;
		}

		if (!userExists) {
			UserEntity ue1 = ub.getUserEntity();
			Calendar calendar = Calendar.getInstance();	
			String newPassword = RandomStringUtils.random(8, true, true);
			// save hashed with salt
			ue1.setPassword(passwordEncoder.getHash(ub.getUsername(), newPassword));
			ue1.setCreatedDate(calendar.getTime());
			ue1.setPasswordChangeNeeded(1);
			em.persist(ue1);
			// we will add a "ROLE_USER" by default
			List<AuthEntity> ael = new ArrayList<AuthEntity>();
			AuthEntity ae = new AuthEntity();
			ae.setAuthority(RoleEntity.ROLE_USER);
			ae.setUsername(ub.getUsername());
			ae.setUser_id(ue1.getId());
			ael.add(ae);
			ue1.setAuthSet(ael);
			// now merge
			em.merge(ue1);
			// default role merged
			newUb = new UserBean(ue1);
			// we set this clear text just this once so that it can be emailed to user
			newUb.setPassword(newPassword);
		} 
		return newUb;
	}

	@Transactional
	public int deleteUser(Long id, String currentUserName) {
		int result = 1;
		
		boolean expenseRecordsExist = true;
		boolean settlementRecordsExist = true;
		UserEntity ue = em.find(UserEntity.class, id);
		
		if (currentUserName.equalsIgnoreCase(ue.getUsername())) {
			return 2;
		}
		// any expense?
		Query getExpensesQuery = em.createNamedQuery("anyExpensesForUser");
		getExpensesQuery.setParameter("userName", ue.getUsername());
		try {
			Collection results = getExpensesQuery.getResultList();
			if (null == results) {
				expenseRecordsExist = false;
			} else {
				if (results.size() == 0) {
					expenseRecordsExist = false;
				}
			}
		} catch (NoResultException nre) {
			expenseRecordsExist = false;
		}
		// any settlements?
		settlementRecordsExist = true;
		Query getAllSettlementsForUserQuery = em
				.createNamedQuery("getAllSettlementsForUser");
		getAllSettlementsForUserQuery
				.setParameter("userName", ue.getUsername());
		try {
			Collection results = getAllSettlementsForUserQuery.getResultList();
			if (null == results) {
				settlementRecordsExist = false;
			} else {
				if (results.size() == 0) {
					settlementRecordsExist = false;
				}
			}
		} catch (NoResultException nre) {
			settlementRecordsExist = false;
		}

		if ((!settlementRecordsExist) && (!expenseRecordsExist)) {
			try {
				UserEntity ue1 = em.find(UserEntity.class, id);
				em.remove(ue1);
//				Query queryDeleteUser = em.createNamedQuery("deleteUser");
//				queryDeleteUser.setParameter("id", id);
//				queryDeleteUser.executeUpdate();
				result = 0;
			} catch (Exception e) {
				result = 1;
			}
		}
		return result;
	}

	public UserBean getUser(String userName) {
		Query queryGetUser = em.createNamedQuery("getUser");
		queryGetUser.setParameter("username", userName);
		UserEntity ue = (UserEntity) queryGetUser.getSingleResult();
		return new UserBean(ue);
	}

	@SuppressWarnings("unchecked")
	public List<UserEntity> getUsers() {
		Query queryGetAllUsers = em.createNamedQuery("getAllUsers");
		Collection users = queryGetAllUsers.getResultList();
		List<UserEntity> userEntitySet = new ArrayList<UserEntity>(users);
		return userEntitySet;
	}

	@Transactional
	public void removeAuthById(Long id) {
	    System.out.println("**** JpaUserService.removeAuth()");
		Query queryRemoveAuthority = em.createNamedQuery("removeAuthority");
		queryRemoveAuthority.setParameter("id", id);
		queryRemoveAuthority.executeUpdate();

	}

	@Transactional
	public UserBean updateAutorization(UserBean ub) {

		Query queryRemoveAuthorities = em.createNamedQuery("removeAuthorities");
		queryRemoveAuthorities.setParameter("username", ub.getUsername());
		queryRemoveAuthorities.executeUpdate();

		UserEntity ue = ub.getUserEntity();
		UserEntity ue0 = new UserEntity();
		ue0 = em.find(UserEntity.class, ub.getId());
		// ue0.setUsername(ue.getUsername());
		// ue0.setPassword(ue.getPassword());
		ue0.setEnabled(ue.getEnabled());
		// grab the auths
		List<AuthEntity> ael = new ArrayList<AuthEntity>();
		for (AuthBean ab : ub.getAuthSet()) {
			AuthEntity ae = new AuthEntity();
			ae.setAuthority(ab.getAuthority());
			ae.setUsername(ab.getUsername());
			ae.setUser_id(ub.getId());
			ael.add(ae);
		}
		// clear out auths first
		// ue0.setAuthSet(new ArrayList<AuthEntity>());
		// em.merge(ue0);
		// em.flush();
		ue0.setAuthSet(ael);
		em.merge(ue0);
		em.flush();
		return new UserBean(ue0);
	}

	@SuppressWarnings("unchecked")
	public List<String> getUserNames() {
		Query queryGetAllUserNames = em.createNamedQuery("getAllUserNames");
		Collection users = queryGetAllUserNames.getResultList();
		List<String> userNameList = new ArrayList<String>(users);
		return userNameList;
	}

	public List<SelectItem> getUserNamesSelectItems() {
		List<SelectItem> ul = new ArrayList<SelectItem>();
		// ul.add(new SelectItem(UserEntity.ALL_USERS,UserEntity.ALL_USERS));
		for (UserEntity ue : getUsers()) {
			if (ue.getEnabled() == UserEntity.USER_ENABLED) {
				boolean thisIsAnUser = false;
				for (AuthEntity ae : ue.getAuthSet()) {
					if (ae.getAuthority().equalsIgnoreCase(RoleEntity.ROLE_USER)) {
						thisIsAnUser = true;
					}
				}
				if (thisIsAnUser) {
					ul.add(new SelectItem(ue.getUsername(), ue.getFirstName()));
				}
			}
		}
		return ul;
	}

	public Collection<UserBean> getUserBeans() {
		List<UserBean> ubs = new ArrayList<UserBean>();
		for (UserEntity ue : getUsers()) {
			ubs.add(new UserBean(ue));
		}
		return ubs;
	}

	@Transactional
	public void updateUser(UserBean ub) {
		UserEntity ue = em.find(UserEntity.class, ub.getId());
		if (ub.getEnabled()) {
			ue.setEnabled(UserEntity.USER_ENABLED);
		} else {
			ue.setEnabled(UserEntity.USER_DISABLED);
		}
		ue.setEmailId(ub.getEmailId());
		ue.setFirstName(ub.getFirstName());
		ue.setMiddleInit(ub.getMiddleInit());
		ue.setLastName(ub.getLastName());
		Calendar calendar = Calendar.getInstance();
		ue.setLastUpdatedDate(calendar.getTime());
		em.merge(ue);
		em.flush();
	}

	@Transactional
	public String changePassword(PasswordChangeBean pB, UserBean ub) {
		String msg = "";
		UserEntity ue = em.find(UserEntity.class, ub.getId());
		String oldPasswordHash = passwordEncoder.getHash(ub.getUsername(), pB.getOldPassword());
		
		if (oldPasswordHash.equalsIgnoreCase(ue.getPassword())) {
			if(pB.getNewPassword().toUpperCase().equalsIgnoreCase(pB.getNewPasswordAgain().toUpperCase())) {
				ue.setPassword(passwordEncoder.getHash(ub.getUsername(), pB.getNewPassword()));
				ue.setPasswordChangeNeeded(UserEntity.PASSWORD_CHANGE_NOT_NEEDED);
				em.merge(ue);
			} else {
				msg = "New passwords do not match.";
			}
		} else
		{
			msg = "Old password is invalid.";
		}
		return msg;
	}

	public boolean isPasswordChangeNeeded(String userName) {
		Query queryGetUser = em.createNamedQuery("getUser");
		queryGetUser.setParameter("username", userName);
		UserEntity ue = (UserEntity) queryGetUser.getSingleResult();
		if (ue.getPasswordChangeNeeded() == UserEntity.PASSWORD_CHANGE_NEEDED) {
			return true;
		} else {
			return false;
		}
	}

	@Transactional
	public UserBean resetPassword(String userName) {
		UserBean ub = null;
		Query queryGetUser = em.createNamedQuery("getUser");
		queryGetUser.setParameter("username", userName);
		UserEntity ue = (UserEntity) queryGetUser.getSingleResult();
		// form password
		String newPassword = RandomStringUtils.random(8, true, true);
		UserEntity ue1 = em.find(UserEntity.class, ue.getId());
		ue1.setPassword(passwordEncoder.getHash(ue.getUsername(), newPassword));
		ue1.setPasswordChangeNeeded(UserEntity.PASSWORD_CHANGE_NEEDED);
		em.merge(ue1);		
		ub = new UserBean(ue1);
		ub.setPassword(newPassword);
		return ub;
	}

	public void setPasswordEncoder(IPasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}

	public void storeUserInSession(ExternalContext ctx, UserBean ub) {
		ctx.getSessionMap().put("thisUser",ub);
	}

}
