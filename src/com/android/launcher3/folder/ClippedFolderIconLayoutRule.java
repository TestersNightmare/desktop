package com.android.launcher3.folder;

public class ClippedFolderIconLayoutRule {

    public static final int MAX_NUM_ITEMS_IN_PREVIEW = 9; // 固定 3x3
    private static final float FIXED_SCALE = 0.25f; // 图标缩放比例固定
    private static final float ITEM_RADIUS_SCALE_FACTOR = 1.15f; // 预留，实际用不到
    public static final int EXIT_INDEX = -2;
    public static final int ENTER_INDEX = -3;
    public static final float ICON_OVERLAP_FACTOR = 1.075f; // 保持不变

    private final float[] mTmpPoint = new float[2];

    private float mAvailableSpace; // 单边空间
    private float mIconSize; // 图标原始尺寸
    private boolean mIsRtl;
    private float mBaselineIconScale;

    public void init(int availableSpace, float intrinsicIconSize, boolean rtl) {
        mAvailableSpace = availableSpace;
        mIconSize = intrinsicIconSize;
        mIsRtl = rtl;
        mBaselineIconScale = availableSpace / (intrinsicIconSize * 1f);
    }


    public PreviewItemDrawingParams computePreviewItemDrawingParams(
            int index, int curNumItems, PreviewItemDrawingParams params) {
        // 整体网格宽度 = 可用空间 × 0.9
        float gridWidth = mAvailableSpace * 0.8f;
        // 单个 cell 的大小
        float cellSize  = gridWidth / 3f;
        // 缩略图占 cell 的比例（再缩小 20%）
        float thumbRatio = 0.8f;
        // 缩放比例：thumbSize / 原始图标大小
        float totalScale = cellSize * thumbRatio / mIconSize;
        // 缩略图绘制时在 cell 内的留白
        float padding    = (cellSize - cellSize * thumbRatio) / 2f;
        // 起始偏移，使网格整个居中
        float offset     = (mAvailableSpace - gridWidth) / 2f;

        // 计算行列
        int row, col;
        if (index == EXIT_INDEX) {
            row = 2; col = 2;
        } else if (index == ENTER_INDEX) {
            row = 1; col = 2;
        } else {
            row = index / 3;
            col = index % 3;
        }

        // 在 cell 内再加上 padding
        float transX = offset + col * cellSize + padding;
        float transY = offset + row * cellSize + padding;

        if (params == null) {
            params = new PreviewItemDrawingParams(transX, transY, totalScale);
        } else {
            params.update(transX, transY, totalScale);
        }
        return params;
    }



    private void getGridPosition(int row, int col, float[] result) {
        float iconSize = mIconSize * scaleForItem(MAX_NUM_ITEMS_IN_PREVIEW);
        // 总空隙份数 = 4
        float spacing = (mAvailableSpace - 3 * iconSize) / 8f;
        // 左/上边距只用 1 份
        float left    = spacing;
        float top     = spacing;

        result[0] = mIsRtl
            ? (mAvailableSpace - (left + (col + 1) * iconSize + col * spacing))
            : (left + col * (iconSize + spacing));
        result[1] = top + row * (iconSize + spacing);
    }

    private void getPosition(int index, float[] result) {
        int row = index / 3;
        int col = index % 3;
        getGridPosition(row, col, result);
    }

    public float scaleForItem(int numItems) {
        return mBaselineIconScale * FIXED_SCALE; // 固定比例
    }

    public float getIconSize() {
        return mIconSize;
    }
}

