package com.bobon.mypace.domain.training



interface ActiveTrainingSession :
    TrainingStateReader,
    TrainingCommandController,
    TrainingMetricsUpdater