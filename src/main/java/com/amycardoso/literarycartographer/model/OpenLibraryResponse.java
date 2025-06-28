package com.amycardoso.literarycartographer.model;

import java.util.List;

public record OpenLibraryResponse(
        List<BookDoc> docs
) {
}
