ALTER TABLE CertificateData ADD tag VARCHAR(255) DEFAULT NULL;
update UserData set certificateProfileId=9 where username='tomcat' and certificateProfileId=1;
ALTER TABLE CertificateData ADD certificateProfileId NUMBER(10) DEFAULT 0;
UPDATE CertificateData SET certificateProfileId=(SELECT certificateProfileId FROM UserData WHERE CertificateData.username=UserData.username);
ALTER TABLE CertificateData ADD updateTime NUMBER(19) DEFAULT 0 NOT NULL;
