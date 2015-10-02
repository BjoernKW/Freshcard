--
-- PostgreSQL database dump
--

-- Dumped from database version 9.3.4
-- Dumped by pg_dump version 9.3beta2
-- Started on 2014-09-06 17:29:49 CEST

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- TOC entry 183 (class 3079 OID 12018)
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- TOC entry 2302 (class 0 OID 0)
-- Dependencies: 183
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- TOC entry 170 (class 1259 OID 25581)
-- Name: authorities; Type: TABLE; Schema: public; Owner: willy; Tablespace: 
--

CREATE TABLE authorities (
    username character varying NOT NULL,
    authority character varying NOT NULL,
    id integer NOT NULL,
    organization_id integer NOT NULL
);


ALTER TABLE public.authorities OWNER TO willy;

--
-- TOC entry 171 (class 1259 OID 25587)
-- Name: authorities_id_seq; Type: SEQUENCE; Schema: public; Owner: willy
--

CREATE SEQUENCE authorities_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.authorities_id_seq OWNER TO willy;

--
-- TOC entry 2303 (class 0 OID 0)
-- Dependencies: 171
-- Name: authorities_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: willy
--

ALTER SEQUENCE authorities_id_seq OWNED BY authorities.id;


--
-- TOC entry 172 (class 1259 OID 25589)
-- Name: contacts_id_seq; Type: SEQUENCE; Schema: public; Owner: willy
--

CREATE SEQUENCE contacts_id_seq
    START WITH 2347524
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.contacts_id_seq OWNER TO willy;

--
-- TOC entry 173 (class 1259 OID 25591)
-- Name: contacts; Type: TABLE; Schema: public; Owner: willy; Tablespace: 
--

CREATE TABLE contacts (
    id integer DEFAULT nextval('contacts_id_seq'::regclass) NOT NULL,
    vcard json,
    hash_code character varying
);


ALTER TABLE public.contacts OWNER TO willy;

--
-- TOC entry 174 (class 1259 OID 25598)
-- Name: contacts_users_id_seq; Type: SEQUENCE; Schema: public; Owner: willy
--

CREATE SEQUENCE contacts_users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.contacts_users_id_seq OWNER TO willy;

--
-- TOC entry 175 (class 1259 OID 25600)
-- Name: contacts_users; Type: TABLE; Schema: public; Owner: willy; Tablespace: 
--

CREATE TABLE contacts_users (
    contact_id integer NOT NULL,
    user_id integer NOT NULL,
    user_is_owner boolean DEFAULT false NOT NULL,
    id integer DEFAULT nextval('contacts_users_id_seq'::regclass) NOT NULL,
    organization_id integer
);


ALTER TABLE public.contacts_users OWNER TO willy;

--
-- TOC entry 176 (class 1259 OID 25605)
-- Name: organizations_id_seq; Type: SEQUENCE; Schema: public; Owner: willy
--

CREATE SEQUENCE organizations_id_seq
    START WITH 2347524
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.organizations_id_seq OWNER TO willy;

--
-- TOC entry 177 (class 1259 OID 25607)
-- Name: organizations; Type: TABLE; Schema: public; Owner: willy; Tablespace: 
--

CREATE TABLE organizations (
    id integer DEFAULT nextval('organizations_id_seq'::regclass) NOT NULL,
    name character varying,
    url character varying,
    hash_code character varying
);


ALTER TABLE public.organizations OWNER TO willy;

--
-- TOC entry 178 (class 1259 OID 25614)
-- Name: organizations_users_id_seq; Type: SEQUENCE; Schema: public; Owner: willy
--

CREATE SEQUENCE organizations_users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.organizations_users_id_seq OWNER TO willy;

--
-- TOC entry 179 (class 1259 OID 25616)
-- Name: organizations_users; Type: TABLE; Schema: public; Owner: willy; Tablespace: 
--

CREATE TABLE organizations_users (
    organization_id integer NOT NULL,
    user_id integer NOT NULL,
    id integer DEFAULT nextval('organizations_users_id_seq'::regclass) NOT NULL
);


ALTER TABLE public.organizations_users OWNER TO willy;

--
-- TOC entry 182 (class 1259 OID 25701)
-- Name: schema_version; Type: TABLE; Schema: public; Owner: willy; Tablespace: 
--

CREATE TABLE schema_version (
    version_rank integer NOT NULL,
    installed_rank integer NOT NULL,
    version character varying(50) NOT NULL,
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);


ALTER TABLE public.schema_version OWNER TO willy;

--
-- TOC entry 180 (class 1259 OID 25620)
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: willy
--

CREATE SEQUENCE users_id_seq
    START WITH 2347524
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.users_id_seq OWNER TO willy;

--
-- TOC entry 181 (class 1259 OID 25622)
-- Name: users; Type: TABLE; Schema: public; Owner: willy; Tablespace: 
--

CREATE TABLE users (
    id integer DEFAULT nextval('users_id_seq'::regclass) NOT NULL,
    username character varying NOT NULL,
    password character varying NOT NULL,
    enabled boolean,
    hash_code character varying,
    current_organization_id integer
);


ALTER TABLE public.users OWNER TO willy;

--
-- TOC entry 2128 (class 2604 OID 25629)
-- Name: id; Type: DEFAULT; Schema: public; Owner: willy
--

ALTER TABLE ONLY authorities ALTER COLUMN id SET DEFAULT nextval('authorities_id_seq'::regclass);


--
-- TOC entry 2282 (class 0 OID 25581)
-- Dependencies: 170
-- Data for Name: authorities; Type: TABLE DATA; Schema: public; Owner: willy
--

COPY authorities (username, authority, id, organization_id) FROM stdin;
admin@freshcard.co	ROLE_USER	9	2
admin@freshcard.co	ROLE_ADMIN	10	2
admin@freshcard.co	ROLE_USER	12	3
\.


--
-- TOC entry 2304 (class 0 OID 0)
-- Dependencies: 171
-- Name: authorities_id_seq; Type: SEQUENCE SET; Schema: public; Owner: willy
--

SELECT pg_catalog.setval('authorities_id_seq', 13, true);


--
-- TOC entry 2285 (class 0 OID 25591)
-- Dependencies: 173
-- Data for Name: contacts; Type: TABLE DATA; Schema: public; Owner: willy
--

COPY contacts (id, vcard, hash_code) FROM stdin;
2	["vcard",[["version",{},"text","4.0"],["n",{},"text",["Doe","John","","",""]],["fn",{},"text","John Doe"],["photo",{"type":"JPEG"},"uri","data:image/jpeg;base64,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"],["org",{},"text",[["Example.com Inc.",""]]],["title",{},"text","Imaginary test person"],["email",{"type":["INTERNET","WORK"],"pref":"1"},"text","johnDoe@example.org"],["tel",{"type":"WORK","pref":"1"},"text","+1 617 555 1212"],["tel",{"type":"WORK"},"text","+1 (617) 555-1234"],["tel",{"type":"CELL"},"text","+1 781 555 1212"],["tel",{"type":"HOME"},"text","+1 202 555 1212"],["adr",{"type":"WORK","group":"item1"},"text",["","","132 Hawthorne St","San Francisco","CA","94107","USA"]],["adr",{"type":"HOME","pref":"1","group":"item2"},"text",["","","Karen House 1-11 Bache's St","London","Greater London","N1 6DL","UK"]],["x-abadr",{"group":"item1"},"unknown","us"],["x-abadr",{"group":"item2"},"unknown","us"],["x-ablabel",{"group":"item3"},"unknown","_$!<HomePage>!$_"],["x-ablabel",{"group":"item4"},"unknown","FOAF"],["x-abrelatednames",{"type":"pref","group":"item5"},"unknown","Jane Doe"],["x-ablabel",{"group":"item5"},"unknown","_$!<Friend>!$_"],["x-abuid",{},"unknown","5AD380FD-B2DE-4261-BA99-DE1D1DB52FBE\\\\\\\\:ABPerson"],["note",{},"text","John Doe has a long and varied history, being documented on more police files than anyone else. Reports of his death are alas numerous."],["url",{"type":"pref","group":"item3"},"uri","http://www.example/com/doe"],["url",{"group":"item4"},"uri","http://www.example.com/Joe/foaf.df"],["categories",{},"text","Work","Test group"],["prodid",{},"text","ez-vcard 0.9.5"]]]	b0a14ebcf7ab3475b2132db1b37a4a438a6acbf434daf489402442149c87cbe4a143ba2773293231
\.


--
-- TOC entry 2305 (class 0 OID 0)
-- Dependencies: 172
-- Name: contacts_id_seq; Type: SEQUENCE SET; Schema: public; Owner: willy
--

SELECT pg_catalog.setval('contacts_id_seq', 2, true);


--
-- TOC entry 2287 (class 0 OID 25600)
-- Dependencies: 175
-- Data for Name: contacts_users; Type: TABLE DATA; Schema: public; Owner: willy
--

COPY contacts_users (contact_id, user_id, user_is_owner, id, organization_id) FROM stdin;
2	2	t	1	2
\.


--
-- TOC entry 2306 (class 0 OID 0)
-- Dependencies: 174
-- Name: contacts_users_id_seq; Type: SEQUENCE SET; Schema: public; Owner: willy
--

SELECT pg_catalog.setval('contacts_users_id_seq', 1, true);


--
-- TOC entry 2289 (class 0 OID 25607)
-- Dependencies: 177
-- Data for Name: organizations; Type: TABLE DATA; Schema: public; Owner: willy
--

COPY organizations (id, name, url, hash_code) FROM stdin;
2	Freshcard	http://freshcard.co	65ba737bda318d7423bc1a7223bb1c431bc2d9c5e52ac1134a3fc355e05ae2e79fd7c03d8e6ebb86
3	Test	\N	85b51dbcd144c5757178160f262ccb68ed319c8b36a6d5401732427fbe459efeb694a009422f5582
\.


--
-- TOC entry 2307 (class 0 OID 0)
-- Dependencies: 176
-- Name: organizations_id_seq; Type: SEQUENCE SET; Schema: public; Owner: willy
--

SELECT pg_catalog.setval('organizations_id_seq', 4, true);


--
-- TOC entry 2291 (class 0 OID 25616)
-- Dependencies: 179
-- Data for Name: organizations_users; Type: TABLE DATA; Schema: public; Owner: willy
--

COPY organizations_users (organization_id, user_id, id) FROM stdin;
2	2	1
3	2	2
\.


--
-- TOC entry 2308 (class 0 OID 0)
-- Dependencies: 178
-- Name: organizations_users_id_seq; Type: SEQUENCE SET; Schema: public; Owner: willy
--

SELECT pg_catalog.setval('organizations_users_id_seq', 2, true);


--
-- TOC entry 2294 (class 0 OID 25701)
-- Dependencies: 182
-- Data for Name: schema_version; Type: TABLE DATA; Schema: public; Owner: willy
--

COPY schema_version (version_rank, installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success) FROM stdin;
1	1	1	<< Flyway Init >>	INIT	<< Flyway Init >>	\N	willy	2014-08-31 21:23:32.119361	0	t
2	2	2	Remove unique constraint on organization name	SQL	V2__Remove_unique_constraint_on_organization_name.sql	-1765185683	willy	2014-09-06 17:20:23.348323	141	t
3	3	3	Remove organization name from authorities	SQL	V3__Remove_organization_name_from_authorities.sql	2089393937	willy	2014-09-06 17:21:50.759013	6	t
4	4	4	Set organization id in authorities to NOT NULL	SQL	V4__Set_organization_id_in_authorities_to_NOT_NULL.sql	-2012122110	willy	2014-09-06 17:29:18.421241	5	t
\.


--
-- TOC entry 2293 (class 0 OID 25622)
-- Dependencies: 181
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: willy
--

COPY users (id, username, password, enabled, hash_code, current_organization_id) FROM stdin;
2	admin@freshcard.co	b706cbcd99dd2ebaafd91044872557c46e4ebbb6184924200c2278b276f36587e71a7502beb40890	t	4b4aeedfbbb1fc4f31ed83e9a626f6b2e15fdae310695a12d618aaba50ebf37be128700152015f12	2
\.


--
-- TOC entry 2309 (class 0 OID 0)
-- Dependencies: 180
-- Name: users_id_seq; Type: SEQUENCE SET; Schema: public; Owner: willy
--

SELECT pg_catalog.setval('users_id_seq', 10, true);


--
-- TOC entry 2137 (class 2606 OID 25719)
-- Name: authorities_authority_username_organization_ui; Type: CONSTRAINT; Schema: public; Owner: willy; Tablespace: 
--

ALTER TABLE ONLY authorities
    ADD CONSTRAINT authorities_authority_username_organization_ui UNIQUE (username, organization_id, authority);


--
-- TOC entry 2139 (class 2606 OID 25633)
-- Name: authorities_id; Type: CONSTRAINT; Schema: public; Owner: willy; Tablespace: 
--

ALTER TABLE ONLY authorities
    ADD CONSTRAINT authorities_id PRIMARY KEY (id);


--
-- TOC entry 2141 (class 2606 OID 25635)
-- Name: contact_id; Type: CONSTRAINT; Schema: public; Owner: willy; Tablespace: 
--

ALTER TABLE ONLY contacts
    ADD CONSTRAINT contact_id PRIMARY KEY (id);


--
-- TOC entry 2143 (class 2606 OID 25637)
-- Name: contacts_users_id; Type: CONSTRAINT; Schema: public; Owner: willy; Tablespace: 
--

ALTER TABLE ONLY contacts_users
    ADD CONSTRAINT contacts_users_id PRIMARY KEY (id);


--
-- TOC entry 2146 (class 2606 OID 25639)
-- Name: contacts_users_uq; Type: CONSTRAINT; Schema: public; Owner: willy; Tablespace: 
--

ALTER TABLE ONLY contacts_users
    ADD CONSTRAINT contacts_users_uq UNIQUE (contact_id, user_id);


--
-- TOC entry 2149 (class 2606 OID 25641)
-- Name: organization_id; Type: CONSTRAINT; Schema: public; Owner: willy; Tablespace: 
--

ALTER TABLE ONLY organizations
    ADD CONSTRAINT organization_id PRIMARY KEY (id);


--
-- TOC entry 2152 (class 2606 OID 25645)
-- Name: organizations_users_id; Type: CONSTRAINT; Schema: public; Owner: willy; Tablespace: 
--

ALTER TABLE ONLY organizations_users
    ADD CONSTRAINT organizations_users_id PRIMARY KEY (id);


--
-- TOC entry 2155 (class 2606 OID 25647)
-- Name: organizations_users_uq; Type: CONSTRAINT; Schema: public; Owner: willy; Tablespace: 
--

ALTER TABLE ONLY organizations_users
    ADD CONSTRAINT organizations_users_uq UNIQUE (organization_id, user_id);


--
-- TOC entry 2164 (class 2606 OID 25709)
-- Name: schema_version_pk; Type: CONSTRAINT; Schema: public; Owner: willy; Tablespace: 
--

ALTER TABLE ONLY schema_version
    ADD CONSTRAINT schema_version_pk PRIMARY KEY (version);


--
-- TOC entry 2158 (class 2606 OID 25649)
-- Name: user_id; Type: CONSTRAINT; Schema: public; Owner: willy; Tablespace: 
--

ALTER TABLE ONLY users
    ADD CONSTRAINT user_id PRIMARY KEY (id);


--
-- TOC entry 2161 (class 2606 OID 25651)
-- Name: users_username_ui; Type: CONSTRAINT; Schema: public; Owner: willy; Tablespace: 
--

ALTER TABLE ONLY users
    ADD CONSTRAINT users_username_ui UNIQUE (username);


--
-- TOC entry 2144 (class 1259 OID 25653)
-- Name: contacts_users_ix; Type: INDEX; Schema: public; Owner: willy; Tablespace: 
--

CREATE INDEX contacts_users_ix ON contacts_users USING btree (contact_id, user_id);


--
-- TOC entry 2147 (class 1259 OID 25699)
-- Name: fki_contacts_user_organization_id; Type: INDEX; Schema: public; Owner: willy; Tablespace: 
--

CREATE INDEX fki_contacts_user_organization_id ON contacts_users USING btree (organization_id);


--
-- TOC entry 2156 (class 1259 OID 25655)
-- Name: fki_users_current_organization_id; Type: INDEX; Schema: public; Owner: willy; Tablespace: 
--

CREATE INDEX fki_users_current_organization_id ON users USING btree (current_organization_id);


--
-- TOC entry 2150 (class 1259 OID 25656)
-- Name: organizations_ix; Type: INDEX; Schema: public; Owner: willy; Tablespace: 
--

CREATE INDEX organizations_ix ON organizations USING btree (name);


--
-- TOC entry 2153 (class 1259 OID 25657)
-- Name: organizations_users_ix; Type: INDEX; Schema: public; Owner: willy; Tablespace: 
--

CREATE INDEX organizations_users_ix ON organizations_users USING btree (organization_id, user_id);


--
-- TOC entry 2162 (class 1259 OID 25711)
-- Name: schema_version_ir_idx; Type: INDEX; Schema: public; Owner: willy; Tablespace: 
--

CREATE INDEX schema_version_ir_idx ON schema_version USING btree (installed_rank);


--
-- TOC entry 2165 (class 1259 OID 25712)
-- Name: schema_version_s_idx; Type: INDEX; Schema: public; Owner: willy; Tablespace: 
--

CREATE INDEX schema_version_s_idx ON schema_version USING btree (success);


--
-- TOC entry 2166 (class 1259 OID 25710)
-- Name: schema_version_vr_idx; Type: INDEX; Schema: public; Owner: willy; Tablespace: 
--

CREATE INDEX schema_version_vr_idx ON schema_version USING btree (version_rank);


--
-- TOC entry 2159 (class 1259 OID 25658)
-- Name: users_ix; Type: INDEX; Schema: public; Owner: willy; Tablespace: 
--

CREATE INDEX users_ix ON users USING btree (username);


--
-- TOC entry 2168 (class 2606 OID 25713)
-- Name: authorities_organization_id; Type: FK CONSTRAINT; Schema: public; Owner: willy
--

ALTER TABLE ONLY authorities
    ADD CONSTRAINT authorities_organization_id FOREIGN KEY (organization_id) REFERENCES organizations(id);


--
-- TOC entry 2167 (class 2606 OID 25664)
-- Name: authorities_username_fk; Type: FK CONSTRAINT; Schema: public; Owner: willy
--

ALTER TABLE ONLY authorities
    ADD CONSTRAINT authorities_username_fk FOREIGN KEY (username) REFERENCES users(username);


--
-- TOC entry 2171 (class 2606 OID 25694)
-- Name: contacts_user_organization_id; Type: FK CONSTRAINT; Schema: public; Owner: willy
--

ALTER TABLE ONLY contacts_users
    ADD CONSTRAINT contacts_user_organization_id FOREIGN KEY (organization_id) REFERENCES organizations(id);


--
-- TOC entry 2169 (class 2606 OID 25669)
-- Name: contacts_users_contact_id; Type: FK CONSTRAINT; Schema: public; Owner: willy
--

ALTER TABLE ONLY contacts_users
    ADD CONSTRAINT contacts_users_contact_id FOREIGN KEY (contact_id) REFERENCES contacts(id);


--
-- TOC entry 2170 (class 2606 OID 25674)
-- Name: contacts_users_user_id; Type: FK CONSTRAINT; Schema: public; Owner: willy
--

ALTER TABLE ONLY contacts_users
    ADD CONSTRAINT contacts_users_user_id FOREIGN KEY (user_id) REFERENCES users(id);


--
-- TOC entry 2172 (class 2606 OID 25679)
-- Name: organizations_users_organization_id; Type: FK CONSTRAINT; Schema: public; Owner: willy
--

ALTER TABLE ONLY organizations_users
    ADD CONSTRAINT organizations_users_organization_id FOREIGN KEY (organization_id) REFERENCES organizations(id);


--
-- TOC entry 2173 (class 2606 OID 25684)
-- Name: organizations_users_user_id; Type: FK CONSTRAINT; Schema: public; Owner: willy
--

ALTER TABLE ONLY organizations_users
    ADD CONSTRAINT organizations_users_user_id FOREIGN KEY (user_id) REFERENCES users(id);


--
-- TOC entry 2174 (class 2606 OID 25689)
-- Name: users_current_organization_id; Type: FK CONSTRAINT; Schema: public; Owner: willy
--

ALTER TABLE ONLY users
    ADD CONSTRAINT users_current_organization_id FOREIGN KEY (current_organization_id) REFERENCES organizations(id);


--
-- TOC entry 2301 (class 0 OID 0)
-- Dependencies: 6
-- Name: public; Type: ACL; Schema: -; Owner: willy
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM willy;
GRANT ALL ON SCHEMA public TO willy;
GRANT ALL ON SCHEMA public TO PUBLIC;


-- Completed on 2014-09-06 17:29:49 CEST

--
-- PostgreSQL database dump complete
--

