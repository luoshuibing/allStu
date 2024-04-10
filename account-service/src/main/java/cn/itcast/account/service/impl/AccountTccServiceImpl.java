package cn.itcast.account.service.impl;

import cn.itcast.account.entity.AccountFreeze;
import cn.itcast.account.mapper.AccountFreezeMapper;
import cn.itcast.account.mapper.AccountMapper;
import cn.itcast.account.service.AccountTccService;
import io.seata.core.context.RootContext;
import io.seata.rm.tcc.api.BusinessActionContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AccountTccServiceImpl implements AccountTccService {

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private AccountFreezeMapper accountFreezeMapper;

    @Override
    public void deduct(String userId, int money) {
        log.info("============================deduct");
        String xid = RootContext.getXID();
        AccountFreeze oldFreeze = accountFreezeMapper.selectById(xid);
        if (oldFreeze != null) {
            return;
        }
        accountMapper.deduct(userId, money);
        AccountFreeze accountFreeze = new AccountFreeze();
        accountFreeze.setUserId(userId);
        accountFreeze.setFreezeMoney(money);
        accountFreeze.setState(AccountFreeze.State.TRY);
        accountFreeze.setXid(xid);
        accountFreezeMapper.insert(accountFreeze);

    }

    @Override
    public boolean confirm(BusinessActionContext context) {
        String xid = context.getXid();
        return accountFreezeMapper.deleteById(xid) == 1;
    }

    @Override
    public boolean cancel(BusinessActionContext context) {
        log.info("============================cancel");
        String xid = context.getXid();
        AccountFreeze accountFreeze = accountFreezeMapper.selectById(xid);
        if (accountFreeze == null) {
            accountFreeze = new AccountFreeze();
            accountFreeze.setUserId(context.getActionContext("userId").toString());
            accountFreeze.setFreezeMoney(0);
            accountFreeze.setState(AccountFreeze.State.CANCEL);
            accountFreeze.setXid(xid);
            accountFreezeMapper.insert(accountFreeze);
            return true;
        }
        if (accountFreeze.getState() == AccountFreeze.State.CANCEL) {
            return true;
        }
        accountMapper.refund(accountFreeze.getUserId(), accountFreeze.getFreezeMoney());
        accountFreeze.setFreezeMoney(0);
        accountFreeze.setState(AccountFreeze.State.CANCEL);
        return accountFreezeMapper.updateById(accountFreeze) == 1;
    }


}
