package com.ensodai.avalon.presentation.chatScreen.component.chat.messageBubble

//@Composable
//fun ImagesBubbleSection(
//    attachedFiles:  List<MessageImageEntity>?,
//    isOnlyImageBubble: Boolean
//) {
//    attachedFiles?.takeIf { it.isNotEmpty() }?.let { attachedList ->
//        val imageCount = attachedList.size
//
//        when (imageCount) {
//            1 -> SingleImage(
//                filePath = attachedList[0],
//                isOnlyImageBubble = isOnlyImageBubble
//            )
//            2 -> TwoImages(
//                attachedList = attachedList,
//                isOnlyImageBubble = isOnlyImageBubble
//            )
//            3 -> ThreeImages(
//                attachedList = attachedList,
//                isOnlyImageBubble = isOnlyImageBubble
//            )
//            else -> GridImages(
//                attachedList = attachedList,
//                isOnlyImageBubble = isOnlyImageBubble
//            )
//        }
//    }
//}
//
//@Composable
//fun SingleImage(filePath: MessageImageEntity, isOnlyImageBubble: Boolean) {
//
//    Image(
//        painter = rememberAsyncImagePainter(
//            model = ImageRequest.Builder(LocalContext.current)
//                .data(File(filePath.imageUrl))
//                .crossfade(true)
//                .build()
//        ),
//        contentDescription = null,
//        contentScale = ContentScale.Crop,
//        modifier = Modifier
//            .padding(2.dp)
//            .height(250.dp)
//            .fillMaxWidth()
//            .clip(
//                RoundedCornerShape(
//                    topStart = 16.dp,
//                    topEnd = 16.dp,
//                    bottomEnd = 8.dp,
//                    bottomStart = if (isOnlyImageBubble) 16.dp else 8.dp,
//                )
//            )
//            .background(Color.LightGray)
//    )
//}
//
//@Composable
//fun TwoImages(attachedList: List<MessageImageEntity>, isOnlyImageBubble: Boolean) {
//
//    Row(
//        modifier = Modifier
//            .padding(
//                start = 2.dp,
//                top = 2.dp,
//                end = 2.dp,
//                bottom = if (isOnlyImageBubble) 2.dp else 0.dp
//            )
//            .fillMaxWidth()
//            .height(250.dp),
//        horizontalArrangement = Arrangement.spacedBy(2.dp)
//    ) {
//        attachedList.forEachIndexed { index, filePath ->
//            val cornerShape = when (index) {
//                0 -> RoundedCornerShape(
//                    topStart = 16.dp,
//                    topEnd = 8.dp,
//                    bottomEnd = 8.dp,
//                    bottomStart = if (isOnlyImageBubble) 16.dp else 8.dp,
//                )
//                else -> RoundedCornerShape(
//                    topStart = 8.dp,
//                    topEnd = 16.dp,
//                    bottomEnd = 8.dp,
//                    bottomStart = 8.dp,
//                )
//            }
//
//            Image(
//                painter = rememberAsyncImagePainter(
//                    model = ImageRequest.Builder(LocalContext.current)
//                        .data(File(filePath.imageUrl))
//                        .crossfade(true)
//                        .build()
//                ),
//                contentDescription = null,
//                contentScale = ContentScale.Crop,
//                modifier = Modifier
//                    .weight(1f)
//                    .height(250.dp)
//                    .clip(cornerShape)
//                    .background(Color.LightGray)
//            )
//        }
//    }
//}
//
//@Composable
//fun ThreeImages(attachedList: List<MessageImageEntity>, isOnlyImageBubble: Boolean) {
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//    ) {
//        Column(
//            modifier = Modifier
//                .padding(
//                    start = 2.dp,
//                    top = 2.dp,
//                    end = 2.dp,
//                    bottom = if (isOnlyImageBubble) 2.dp else 0.dp
//                )
//                .fillMaxWidth()
//        ) {
//            val firstFilePath = attachedList[0]
//            Image(
//                painter = rememberAsyncImagePainter(
//                    model = ImageRequest.Builder(LocalContext.current)
//                        .data(File(firstFilePath.imageUrl))
//                        .crossfade(true)
//                        .build()
//                ),
//                contentDescription = null,
//                contentScale = ContentScale.Crop,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(250.dp)
//                    .clip(
//                        RoundedCornerShape(
//                            topStart = 16.dp,
//                            topEnd = 16.dp,
//                            bottomEnd = 8.dp,
//                            bottomStart = 8.dp,
//                        )
//                    )
//                    .background(Color.LightGray)
//            )
//
//            Spacer(modifier = Modifier.height(2.dp))
//
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth(),
//                horizontalArrangement = Arrangement.spacedBy(2.dp)
//            ) {
//                attachedList.subList(1, 3).forEachIndexed { index, filePath ->
//                    Image(
//                        painter = rememberAsyncImagePainter(
//                            model = ImageRequest.Builder(LocalContext.current)
//                                .data(File(filePath.imageUrl))
//                                .crossfade(true)
//                                .build()
//                        ),
//                        contentDescription = null,
//                        contentScale = ContentScale.Crop,
//                        modifier = Modifier
//                            .weight(1f)
//                            .height(200.dp)
//                            .clip(
//                                RoundedCornerShape(
//                                    topStart = 8.dp,
//                                    topEnd = 8.dp,
//                                    bottomEnd = 8.dp,
//                                    bottomStart = if (index == 0 && isOnlyImageBubble) 16.dp else 8.dp,
//                                )
//                            )
//                            .background(Color.LightGray)
//                    )
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun GridImages(attachedList: List<MessageImageEntity>, isOnlyImageBubble: Boolean) {
//    if (attachedList.isEmpty()) {
//        return
//    }
//
//    val imageSize: Dp = 180.dp
//    val columns = 2
//    val visibleItems = columns * 2
//
//    val lazyListState = rememberLazyGridState()
//    val firstVisibleItemIndex by remember { derivedStateOf { lazyListState.firstVisibleItemIndex } }
//    val unseenCount = maxOf(attachedList.size - (firstVisibleItemIndex + visibleItems), 0)
//
//    val desiredHeight = (((visibleItems / columns).coerceAtMost(2)) * imageSize.value + 4).dp
//    val boxCornerShape = RoundedCornerShape(
//        topStart = 16.dp,
//        topEnd = 16.dp,
//        bottomEnd = 8.dp,
//        bottomStart = if (isOnlyImageBubble) 16.dp else 8.dp
//    )
//
//    Box(
//        modifier = Modifier
//            .padding(2.dp)
//            .aspectRatio(1f)
//            .clip(boxCornerShape)
//    ) {
//        LazyHorizontalGrid(
//            rows = GridCells.Fixed(columns),
//            state = lazyListState,
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(desiredHeight),
//            horizontalArrangement = Arrangement.spacedBy(2.dp),
//            verticalArrangement = Arrangement.spacedBy(2.dp),
//        ) {
//            items(count = attachedList.size) { index ->
//                val filePath = attachedList[index]
//                val cornerShape = RoundedCornerShape(8.dp)
//
//                Image(
//                    painter = rememberAsyncImagePainter(
//                        model = ImageRequest.Builder(LocalContext.current)
//                            .data(File(filePath.imageUrl))
//                            .crossfade(true)
//                            .build()
//                    ),
//                    contentDescription = null,
//                    contentScale = ContentScale.Crop,
//                    modifier = Modifier
//                        .aspectRatio(1f)
//                        .clip(cornerShape)
//                        .background(Color.LightGray)
//                )
//            }
//        }
//
//        // Иконка с динамическим числом оставшихся непросмотренных изображений
//        if (unseenCount > 0) {
//            Row(
//                modifier = Modifier
//                    .align(Alignment.CenterEnd)
//                    .padding(8.dp)
//                    .background(Color.Black.copy(alpha = 0.6f), shape = RoundedCornerShape(16.dp))
//                    .padding(horizontal = 10.dp, vertical = 6.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Text(
//                    text = "+$unseenCount",
//                    color = Color.White,
//                    style = MaterialTheme.typography.bodyMedium,
//                    textAlign = TextAlign.Center
//                )
//                Spacer(modifier = Modifier.width(4.dp))
//                Icon(
//                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
//                    contentDescription = "Показать еще",
//                    tint = Color.White,
//                    modifier = Modifier.size(16.dp)
//                )
//            }
//        }
//    }
//}
