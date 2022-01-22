package ca.vanzyl.provisio.tools.util;

import io.quarkus.runtime.annotations.RegisterForReflection;
import org.apache.commons.compress.archivers.zip.X000A_NTFS;
import org.apache.commons.compress.archivers.zip.X0014_X509Certificates;
import org.apache.commons.compress.archivers.zip.X0015_CertificateIdForFile;
import org.apache.commons.compress.archivers.zip.X0016_CertificateIdForCentralDirectory;
import org.apache.commons.compress.archivers.zip.X0017_StrongEncryptionHeader;
import org.apache.commons.compress.archivers.zip.X0019_EncryptionRecipientCertificateList;
import org.apache.commons.compress.archivers.zip.X5455_ExtendedTimestamp;
import org.apache.commons.compress.archivers.zip.X7875_NewUnix;

@RegisterForReflection(targets = {
    X5455_ExtendedTimestamp.class,
    X7875_NewUnix.class,
    X000A_NTFS.class,
    X0014_X509Certificates.class,
    X0015_CertificateIdForFile.class,
    X0016_CertificateIdForCentralDirectory.class,
    X0017_StrongEncryptionHeader.class,
    X0019_EncryptionRecipientCertificateList.class
})
public class ProvisioReflectionConfiguration {}
