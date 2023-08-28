package com.beagledata.gaea.workbench.controller;

import com.beagledata.common.Result;
import com.beagledata.gaea.workbench.service.LicenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("license")
public class LicenseController {
	@Autowired
	private LicenseService licenseService;

	@GetMapping("sn")
    public Result getSn() {
        return Result.newSuccess().withData(licenseService.getSn());
    }
    
    @PostMapping("upload")
    public Result upload(@RequestBody MultipartFile file) {
        licenseService.upload(file);
        return Result.SUCCESS;
    }
}
