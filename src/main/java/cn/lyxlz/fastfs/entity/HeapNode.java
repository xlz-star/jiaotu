package cn.lyxlz.demo;

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
    private volatile int val;
    /**
     * 左节点
     */
    private volatile HeapNode left;
    /**
     * 右节点
     */
    private volatile HeapNode right;

    public HeapNode(int val) {
        this.val = val;
    }

    /**
     * 插入
     *
     * @param num 节点的值
     */
    public synchronized void insert(int num) {
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
                if (this.left.val < this.right.val) {
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
        // 找到最左节点
        HeapNode leftNode = this;
        while (leftNode.left != null) {
            leftNode = leftNode.left;
        }
        // 将最左节点的值放到根
        this.val = leftNode.val;
        // 删除最左节点
        leftNode = null;
        heapify();
    }

    public synchronized int getHeapTop() {
        return this.val;
    }

    /**
     * 建立堆
     */
    private synchronized void heapify() {
        // 如果左子节点不为空且左子节点的值大于当前节点的值，则交换左子节点和当前节点的值
        if (this.left != null && this.left.val > this.val) {
            swap(this, this.left);
            this.left.heapify();
        }

        // 如果右子节点不为空且右子节点的值大于当前节点的值，则交换右子节点和当前节点的值
        if (this.right != null && this.right.val > this.val) {
            swap(this, this.right);
            this.right.heapify();
        }
    }


    /**
     * 交换两个节点的值
     *
     * @param node1 节点1
     * @param node2 节点2
     */
    private synchronized void swap(HeapNode node1, HeapNode node2) {
        int temp = node1.val;
        node1.val = node2.val;
        node2.val = temp;
    }


    /**
     * 打印堆
     */
    public synchronized void printHeap() {
        inorderTraversal(this);
    }


    /**
     * 中序遍历返回堆的节点值
     *
     * @param node 节点
     */
    private synchronized void inorderTraversal(HeapNode node) {
        if (node == null) {
            return;
        }
        System.out.print(node.val + " ");
        inorderTraversal(node.left);
        inorderTraversal(node.right);
    }
}
