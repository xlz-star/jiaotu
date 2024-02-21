package cn.lyxlz.fastfs.entity;

import cn.hutool.core.util.ObjUtil;

import java.io.Serializable;

/**
 * 大根堆节点
 *
 * @author xlz
 * @date 2023/12/06
 */
public class HeapNode implements Serializable {
    private static final long serialVersionUID = 3136173178499687329L;

    /**
     * 节点值
     */
    public volatile Node val;

    /**
     * 左节点
     */
    public volatile HeapNode left;

    /**
     * 右节点
     */
    public volatile HeapNode right;

    public HeapNode(Node val) {
        this.val = val;
    }


    /**
     * 堆中节点个数
     *
     * @return
     */
    public int size() {
        return this.countNode(this);
    }

    /**
     * 堆中节点个数
     *
     * @param root
     * @return
     */
    private int countNode(HeapNode root) {
        if (root == null) {
            return 0;
        }
        return 1 + countNode(root.left) + countNode(root.right);
    }

    /**
     * 统计堆中各节点的剩余容量
     *
     * @return
     */
    public long sumFree() {
        return this.sumFree(this);
    }

    /**
     * 统计堆中各节点的剩余容量
     *
     * @param root
     * @return
     */
    private long sumFree(HeapNode root) {
        if (root == null) {
            return 0;
        }
        return root.val.getFree() + sumFree(root.left) + sumFree(root.right);
    }


    /**
     * 插入
     *
     * @param num 节点的值
     */
    public synchronized void insert(Node num) {
        HeapNode newNode = new HeapNode(num);

        // 判断左子节点是否为空，如果为空，则将新节点作为左子节点
        if (this.left == null) {
            this.left = newNode;
        } else {
            // 判断右子节点是否为空，如果为空，则将新节点作为右子节点
            if (this.right == null) {
                this.right = newNode;
            } else {
                // 左右子节点都已经存在，通过比较选择一个较小的子节点进行插入
                if (toBool(this.left.val.compareTo(this.right.val))) {
                    this.left.insert(num);
                } else {
                    this.right.insert(num);
                }
            }
        }

        // 调整堆结构
        heapify();
    }

    /**
     * 删除堆顶节点
     */
    public synchronized void deleteHeapTop() {
        // 交换堆顶元素和最后一个元素
        HeapNode root = this;
        swap(root, getLastNode(root));
        // 移除最后一个元素
        removeLastNode(root);
        heapify();
    }

    // 移除最后一个节点，并返回新的堆顶元素
    private HeapNode removeLastNode(HeapNode root) {
        if (root == null) {
            return null;
        }
        if (root.right != null) {
            if (root.right.right == null && root.right.left == null) {
                root.right = null;
            } else {
                return removeLastNode(root.right);
            }
        } else if (root.left != null) {
            if (root.left.right == null && root.left.left == null) {
                root.left = null;
            } else {
                return removeLastNode(root.left);
            }
        } else {
            return null;
        }
        return root;
    }

    private HeapNode getLastNode(HeapNode root) {
        if (root == null) {
            return null;
        }
        if (root.right != null) {
            return getLastNode(root.right);
        }
        if (root.left != null) {
            return getLastNode(root.left);
        }
        return root;
    }

    public synchronized Node getHeapTop() {
        return this.val;
    }

    /**
     * 建立堆
     */
    public synchronized void heapify() {
        // 如果左子节点不为空且左子节点的值大于当前节点的值，则交换左子节点和当前节点的值
        if (this.left != null && toBool(this.left.val.compareTo(this.val))) {
            swap(this, this.left);
            this.left.heapify();
        }

        // 如果右子节点不为空且右子节点的值大于当前节点的值，则交换右子节点和当前节点的值
        if (this.right != null && toBool(this.right.val.compareTo(this.val))) {
            swap(this, this.right);
            this.right.heapify();
        }
    }

    /**
     * 是否包含节点
     *
     * @param node
     * @return boolean
     */
    public synchronized boolean containsNode(Node node) {
        if (ObjUtil.equals(this.val, node)) {
            return true;
        } else {
            boolean containsLeft = (left != null) && left.containsNode(node);
            boolean containsRight = (right != null) && right.containsNode(node);
            return containsLeft || containsRight;
        }
    }

    /**
     * 交换两个节点的值
     *
     * @param node1 节点1
     * @param node2 节点2
     */
    private synchronized void swap(HeapNode node1, HeapNode node2) {
        Node temp = node1.val;
        node1.val = node2.val;
        node2.val = temp;
    }


    /**
     * 如果整数>0则返回true
     *
     * @param i
     * @return boolean
     */
    private boolean toBool(int i) {
        return i > 0;
    }


}
