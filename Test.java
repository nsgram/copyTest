private byte[] getDummyPdfBytes() {
    return "%PDF-1.4\n%âãÏÓ\n1 0 obj\n<< /Type /Catalog >>\nendobj\nxref\n0 1\n0000000000 65535 f \ntrailer\n<< /Root 1 0 R >>\nstartxref\n9\n%%EOF".getBytes(StandardCharsets.UTF_8);
}
