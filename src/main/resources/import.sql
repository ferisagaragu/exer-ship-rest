/*USERS*/
INSERT INTO public.users (uid, email, name, last_name, password, user_name) VALUES ('a411b670-df80-4fe0-8668-967f3f7bf502','ferisagaragu@gmail.com','Fernando', 'Garcia', '$2a$10$jHQoVkuthPmpNH608IkGNOWZidPFXJQs9BPy8ncZL3aRgswuxLNMC','fernnypay95');
INSERT INTO public.users (uid, email, name, last_name, password, user_name) VALUES ('4a153ec6-ee3d-4be0-9034-ebe629b1e388','ale@gmail.com','Alejandra', 'Lozano', '$2a$10$jHQoVkuthPmpNH608IkGNOWZidPFXJQs9BPy8ncZL3aRgswuxLNMC','aleOfe95');

/*TEAMS*/
INSERT INTO public.teams(uid, name) VALUES ('5f3f5585-37f4-4100-9042-3189ac608013', 'Los Morados 💜');

/*TEAMS_USERS*/
INSERT INTO public.teams_users(team_uid, users_uid) VALUES ('5f3f5585-37f4-4100-9042-3189ac608013', 'a411b670-df80-4fe0-8668-967f3f7bf502');
INSERT INTO public.teams_users(team_uid, users_uid) VALUES ('5f3f5585-37f4-4100-9042-3189ac608013', '4a153ec6-ee3d-4be0-9034-ebe629b1e388');