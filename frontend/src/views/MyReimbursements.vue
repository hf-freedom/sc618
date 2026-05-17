<template>
  <div class="my-reimbursements">
    <el-card shadow="hover">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>我的报销</span>
          <el-button type="primary" size="small" @click="$router.push('/create')">新建报销</el-button>
        </div>
      </template>
      <el-table :data="reimbursementList" border stripe>
        <el-table-column prop="id" label="报销单号" width="250" />
        <el-table-column prop="expenseType" label="费用类型" width="120">
          <template #default="scope">
            {{ getExpenseTypeName(scope.row.expenseType) }}
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" width="100">
          <template #default="scope">
            ¥{{ scope.row.amount }}
          </template>
        </el-table-column>
        <el-table-column prop="description" label="费用说明" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusName(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="submitTime" label="提交时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="viewDetail(scope.row)">查看</el-button>
            <el-button
              v-if="scope.row.status === 'REJECTED'"
              size="small"
              type="warning"
              @click="resubmit(scope.row)"
            >
              修改重提
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="detailVisible" title="报销详情" width="600px">
      <el-descriptions :column="1" border v-if="currentReimbursement">
        <el-descriptions-item label="报销单号">{{ currentReimbursement.id }}</el-descriptions-item>
        <el-descriptions-item label="员工姓名">{{ currentReimbursement.employeeName }}</el-descriptions-item>
        <el-descriptions-item label="所属部门">{{ currentReimbursement.department }}</el-descriptions-item>
        <el-descriptions-item label="费用类型">{{ getExpenseTypeName(currentReimbursement.expenseType) }}</el-descriptions-item>
        <el-descriptions-item label="报销金额">¥{{ currentReimbursement.amount }}</el-descriptions-item>
        <el-descriptions-item label="费用说明">{{ currentReimbursement.description }}</el-descriptions-item>
        <el-descriptions-item label="发票编号">{{ currentReimbursement.invoiceNumber }}</el-descriptions-item>
        <el-descriptions-item label="当前状态">
          <el-tag :type="getStatusType(currentReimbursement.status)">
            {{ getStatusName(currentReimbursement.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item v-if="currentReimbursement.rejectReason" label="驳回原因">
          <el-tag type="danger">{{ currentReimbursement.rejectReason }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="resubmitVisible" title="修改重新提交" width="600px">
      <el-form :model="resubmitForm" label-width="100px">
        <el-form-item label="费用类型">
          <el-select v-model="resubmitForm.expenseType" style="width: 100%">
            <el-option label="交通费" value="TRANSPORTATION" />
            <el-option label="住宿费" value="ACCOMMODATION" />
            <el-option label="餐饮费" value="MEAL" />
            <el-option label="办公用品" value="OFFICE_SUPPLY" />
            <el-option label="差旅费" value="TRAVEL" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="报销金额">
          <el-input-number v-model="resubmitForm.amount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="费用说明">
          <el-input v-model="resubmitForm.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="发票编号">
          <el-input v-model="resubmitForm.invoiceNumber" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resubmitVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmResubmit">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'MyReimbursements',
  data() {
    return {
      reimbursementList: [],
      detailVisible: false,
      resubmitVisible: false,
      currentReimbursement: null,
      resubmitForm: {
        id: '',
        expenseType: '',
        amount: 0,
        description: '',
        invoiceNumber: ''
      },
      currentUserId: 'E001'
    }
  },
  mounted() {
    this.loadCurrentUser()
    this.loadMyReimbursements()
  },
  activated() {
    this.loadCurrentUser()
    this.loadMyReimbursements()
  },
  watch: {
    $route(to) {
      if (to.path === '/my') {
        this.loadCurrentUser()
        this.loadMyReimbursements()
      }
    }
  },
  methods: {
    loadCurrentUser() {
      const app = this.$root
      if (app.$children && app.$children[0]) {
        const mainApp = app.$children[0]
        if (mainApp.currentUserId) {
          this.currentUserId = mainApp.currentUserId
        }
      }
    },
    async loadMyReimbursements() {
      try {
        const response = await axios.get(`http://localhost:8003/api/reimbursement/employee/${this.currentUserId}`)
        this.reimbursementList = response.data
      } catch (error) {
        console.error('加载报销列表失败', error)
      }
    },
    getExpenseTypeName(type) {
      const names = {
        TRANSPORTATION: '交通费',
        ACCOMMODATION: '住宿费',
        MEAL: '餐饮费',
        OFFICE_SUPPLY: '办公用品',
        TRAVEL: '差旅费',
        OTHER: '其他'
      }
      return names[type] || type
    },
    getStatusName(status) {
      const names = {
        DRAFT: '草稿',
        PENDING_APPROVAL: '待审批',
        MANAGER_APPROVED: '经理已批准',
        FINANCE_REVIEW: '财务复核',
        APPROVED: '已批准',
        REJECTED: '已驳回',
        PAYMENT_PENDING: '待打款',
        COMPLETED: '已完成'
      }
      return names[status] || status
    },
    getStatusType(status) {
      const types = {
        DRAFT: 'info',
        PENDING_APPROVAL: 'warning',
        MANAGER_APPROVED: 'primary',
        FINANCE_REVIEW: 'primary',
        APPROVED: 'success',
        REJECTED: 'danger',
        PAYMENT_PENDING: 'warning',
        COMPLETED: 'success'
      }
      return types[status] || 'info'
    },
    viewDetail(row) {
      this.currentReimbursement = row
      this.detailVisible = true
    },
    resubmit(row) {
      this.currentReimbursement = row
      this.resubmitForm = {
        id: row.id,
        expenseType: row.expenseType,
        amount: row.amount,
        description: row.description,
        invoiceNumber: row.invoiceNumber
      }
      this.resubmitVisible = true
    },
    async confirmResubmit() {
      try {
        await axios.post(
          `http://localhost:8003/api/reimbursement/resubmit/${this.resubmitForm.id}`,
          this.resubmitForm
        )
        this.$message.success('重新提交成功')
        this.resubmitVisible = false
        this.loadMyReimbursements()
      } catch (error) {
        this.$message.error(error.response?.data?.message || '提交失败')
      }
    }
  }
}
</script>

<style scoped>
.my-reimbursements {
  padding: 0;
}
</style>
