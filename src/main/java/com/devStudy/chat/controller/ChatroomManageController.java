package com.devStudy.chat.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.devStudy.chat.dto.ChatMsgDTO;
import com.devStudy.chat.dto.ChatroomRequestDTO;
import com.devStudy.chat.dto.ModifyChatroomDTO;
import com.devStudy.chat.dto.ModifyChatroomRequestDTO;
import com.devStudy.chat.dto.UserDTO;
import com.devStudy.chat.service.implementations.ChatMessageService;
import com.devStudy.chat.service.implementations.ChatroomService;
import com.devStudy.chat.service.implementations.UserChatroomRelationService;
import com.devStudy.chat.service.implementations.UserService;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/chatrooms")
public class ChatroomManageController {

	private final Logger logger = LoggerFactory.getLogger(ChatroomManageController.class);

	@Resource
	private UserService userService;

	@Resource
	private ChatroomService chatroomService;

	@Resource
	private UserChatroomRelationService userChatroomRelationService;

	@Resource
	private ChatMessageService chatMessageService;

	/**
	 * Cettet méthode permet créer une chatroom Si une chatroom existe déjà (ou il y
	 * a des informations conflicts que les chatrooms existantes), on va retourner
	 * un code 409 (conflit)
	 */
	@PostMapping("/create")
	public ResponseEntity<Boolean> createChatroom(@RequestBody ChatroomRequestDTO chatroomRequestDTO) {
		if (userService.checkUserLoginStatus()) {
			UserDTO user = userService.getLoggedUser();
			boolean result = chatroomService.createChatroom(chatroomRequestDTO, user.getId());
			if (result) {
				return ResponseEntity.ok(true);
			} else {
				return ResponseEntity.status(409).body(false);
			}
		}
		return ResponseEntity.status(401).body(false);
	}

	/**
	 * Cette méthode permet de supprimer une chatroom Si il y a des conflits pendant
	 * la suppression, on va retourner un code 409 (conflit)
	 */
	@DeleteMapping("/{chatroomId}")
	public ResponseEntity<Boolean> deleteChatroom(@PathVariable long chatroomId) {
		if (userService.checkUserLoginStatus()) {
			boolean checkOwner = chatroomService.checkUserIsOwnerOfChatroom(userService.getLoggedUser().getId(),
					chatroomId);
			if (checkOwner) {
				if (chatroomService.deleteChatRoom(chatroomId)) {
					return ResponseEntity.ok(true);
				} else {
					return ResponseEntity.status(409).body(false);
				}
			} else {
				return ResponseEntity.status(403).body(false);
			}
		}
		return ResponseEntity.status(401).body(false);
	}

	/**
	 * Cette méthode permet d'obtenir les informations d'une chatroom Si le chatroom
	 * n'existe pas, on va retourner un code 404 (non trouvé)
	 */
	@GetMapping("/{chatroomId}")
	public ResponseEntity<ModifyChatroomDTO> getChatroomForModify(@PathVariable long chatroomId) {
		if (userService.checkUserLoginStatus()) {
			Optional<ModifyChatroomDTO> chatroom = chatroomService.findChatroom(chatroomId);
			boolean checkOwner = chatroomService.checkUserIsOwnerOfChatroom(userService.getLoggedUser().getId(),
					chatroomId);
			return checkOwner
					? chatroom.map(value -> ResponseEntity.ok(value))
							.orElseGet(() -> ResponseEntity.status(404).body(new ModifyChatroomDTO()))
					: ResponseEntity.status(403).body(new ModifyChatroomDTO());
		}
		return ResponseEntity.status(401).body(new ModifyChatroomDTO());
	}

	/**
	 * Cette méthode permet d'obtenir tous les utilisateurs invités à une chatroom
	 * on va vérifier si l'utilisateur connecté est le propriétaire de la chatroom
	 * Si oui, on va retourner les utilisateurs invités à cette chatroom, sinon on
	 * va retourner un code 403 (interdit)
	 */
	@GetMapping("/{chatroomId}/users/invited")
	public ResponseEntity<Page<UserDTO>> getUsersInvitedInChatroom(@PathVariable long chatroomId,
			@RequestParam(defaultValue = "0") int page) {
		boolean checkOwner = chatroomService.checkUserIsOwnerOfChatroom(userService.getLoggedUser().getId(),
				chatroomId);
		if (userService.checkUserLoginStatus() && checkOwner) {
			Page<UserDTO> users = userService.findUsersInvitedToChatroomByPage(chatroomId, page);
			return ResponseEntity.ok(users);
		} else if (!checkOwner) {
			return ResponseEntity.status(403).body(Page.empty());
		}
		return ResponseEntity.status(401).body(Page.empty());
	}

	/**
	 * Cette méthode permet d'obtenir tous les utilisateurs non invités à une
	 * chatroom on va vérifier si l'utilisateur connecté est le propriétaire de la
	 * chatroom Si oui, on va retourner les utilisateurs non invités, sinon on va
	 * retourner un code 403 (interdit)
	 */
	@GetMapping("/{chatroomId}/users/not-invited")
	public ResponseEntity<Page<UserDTO>> getUsersNotInvitedInChatroom(@PathVariable long chatroomId,
			@RequestParam(defaultValue = "0") int page) {
		boolean checkOwner = chatroomService.checkUserIsOwnerOfChatroom(userService.getLoggedUser().getId(),
				chatroomId);
		if (userService.checkUserLoginStatus() && checkOwner) {
			Page<UserDTO> users = userService.findUsersNotInvitedToChatroomByPage(chatroomId, page);
			return ResponseEntity.ok(users);
		} else if (!checkOwner) {
			return ResponseEntity.status(403).body(Page.empty());
		}
		return ResponseEntity.status(401).body(Page.empty());
	}

	@PutMapping("/{chatroomId}")
	public ResponseEntity<Boolean> updateChatroomDetails(@PathVariable long chatroomId,
			@RequestBody ModifyChatroomRequestDTO chatroomRequest) {
		boolean checkOwner = chatroomService.checkUserIsOwnerOfChatroom(userService.getLoggedUser().getId(),
				chatroomId);
		if (userService.checkUserLoginStatus() && checkOwner) {
			boolean res = chatroomService.updateChatroom(chatroomRequest, chatroomId);
			if (res) {
				return ResponseEntity.ok(true);
			} else {
				return ResponseEntity.status(409).body(false);
			}
		} else if (!checkOwner) {
			return ResponseEntity.status(403).body(false);
		}
		return ResponseEntity.status(401).body(false);
	}

	/*
	 * Cette méthode permet qu'un utilisateur quitte une chatroom
	 */
	@DeleteMapping("/{chatroomId}/users/invited/{userId}")
	public ResponseEntity<Boolean> leaveChatroom(@PathVariable long chatroomId, @PathVariable long userId) {
		if (userService.checkUserLoginStatus()) {
			boolean res = chatroomService.deleteUserInvited(chatroomId, userId);
			if (res) {
				return ResponseEntity.ok(true);
			} else {
				return ResponseEntity.status(500).body(false);
			}
		}
		return ResponseEntity.status(401).body(false);
	}

	/**
	 * Cette méthode permet d'obtenir tous les utilisateurs dans une chatroom Elle
	 * va etre utilisé dans la page de chatroom pour afficher tous les utilisateurs
	 * dans cette chatroom
	 */
	@GetMapping("/{chatroomId}/members")
	public ResponseEntity<List<UserDTO>> getAllMembersInChatroom(@PathVariable long chatroomId) {
		if (userService.checkUserLoginStatus()) {
			List<UserDTO> users = chatroomService.getAllUsersInChatroom(chatroomId);
			if (users.size() > 0) {
				return ResponseEntity.ok(users);
			} else {
				return ResponseEntity.status(500).body(new ArrayList<>());
			}
		}
		return ResponseEntity.status(401).body(new ArrayList<>());
	}

	/*
	 * Cette méthode permet d'obtenir l'historique des messages dans une chatroom
	 * par le numero de page
	 */
	@GetMapping("/{chatroomId}/history")
	public ResponseEntity<List<ChatMsgDTO>> getHistoryMsgByChatroomIdAndPage(@PathVariable long chatroomId,
			@RequestParam(defaultValue = "0") int page) {
		if (userService.checkUserLoginStatus()) {
			return ResponseEntity.ok(chatMessageService.getChatMessagesByChatroomIdByPage(chatroomId, page));
		}
		return ResponseEntity.status(401).body(new ArrayList<>());
	}
}
