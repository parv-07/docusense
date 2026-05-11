package com.parv.docqa.service;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentService {

    public String extractFile(MultipartFile multipartFile) throws Exception{
        BodyContentHandler bodyContentHandler = new BodyContentHandler();
        Metadata metadata = new Metadata();
        AutoDetectParser parser = new AutoDetectParser();
        parser.parse(multipartFile.getInputStream(),bodyContentHandler, metadata);
        return bodyContentHandler.toString();
    }
}
